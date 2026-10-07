#!/usr/bin/env python3
"""Synthesises every sound of the mod from scratch (no samples) and writes mono Ogg Vorbis files and sounds.json.
Run from the repo root; needs numpy, scipy and ffmpeg. Pass a directory to also get a sheet of spectrograms."""
import json
import os
import subprocess
import sys
import tempfile

import numpy as np
from scipy import signal
from scipy.io import wavfile

SR = 44100
A = 'src/main/resources/assets/vsratie_dushi'
MOD = 'vsratie_dushi'
rng = np.random.default_rng(20261004)


def t(dur):
    return np.arange(int(SR * dur)) / SR


def noise(dur):
    return rng.standard_normal(int(SR * dur))


def env(dur, attack, release, hold=1.0):
    n = int(SR * dur)
    e = np.ones(n) * hold
    a = max(1, min(n, int(SR * attack)))
    r = max(1, min(n, int(SR * release)))
    e[:a] *= np.linspace(0, 1, a) ** 1.5
    e[-r:] *= np.linspace(1, 0, r) ** 1.5
    return e


def decay(dur, tau):
    return np.exp(-t(dur) / tau)


def bandpass(x, lo, hi, order=2):
    sos = signal.butter(order, [max(20, lo), min(SR / 2 - 100, hi)], btype='band', fs=SR, output='sos')
    return signal.sosfilt(sos, x)


def lowpass(x, f, order=2):
    return signal.sosfilt(signal.butter(order, f, btype='low', fs=SR, output='sos'), x)


def highpass(x, f, order=2):
    return signal.sosfilt(signal.butter(order, f, btype='high', fs=SR, output='sos'), x)


def tone(freq, dur, phase=0.0):
    f = np.full(int(SR * dur), float(freq)) if np.isscalar(freq) else freq
    return np.sin(2 * np.pi * np.cumsum(f) / SR + phase)


def saw(freq, dur):
    f = np.full(int(SR * dur), float(freq)) if np.isscalar(freq) else freq
    return signal.sawtooth(2 * np.pi * np.cumsum(f) / SR)


def sweep(f0, f1, dur, curve=1.0):
    return f0 + (f1 - f0) * (t(dur) / dur) ** curve


def moving_bandpass(x, centre, width=0.35, steps=40):
    out = np.zeros(len(x))
    n = len(x)
    hop = n // steps + 1
    win = np.hanning(hop * 2)
    for i in range(0, n, hop):
        a, b = max(0, i - hop // 2), min(n, i + hop + hop // 2)
        c = float(centre[min(n - 1, i + hop // 2)])
        seg = bandpass(x[a:b], c * (1 - width), c * (1 + width))
        w = np.interp(np.arange(b - a), np.linspace(0, b - a, len(win)), win)
        out[a:b] += seg * w
    return out


def reverb(x, seconds=0.7, mix=0.3, tail=None, bright=5000):
    n = int(SR * seconds)
    ir = rng.standard_normal(n) * np.exp(-np.arange(n) / (SR * seconds / 4.5))
    ir = lowpass(ir, bright)
    ir /= np.sqrt(np.sum(ir ** 2))
    pad = np.concatenate([x, np.zeros(n if tail is None else int(SR * tail))])
    wet = signal.fftconvolve(pad, ir)[:len(pad)]
    return pad * (1 - mix) + wet * mix


def place(total, *parts):
    out = np.zeros(int(SR * total))
    for at, x in parts:
        i = int(SR * at)
        x = x[:max(0, len(out) - i)]
        out[i:i + len(x)] += x
    return out


def norm(x, peak=1.0):
    return x / (np.max(np.abs(x)) + 1e-9) * peak


def voice(freq, dur, formants, breath=0.25, jitter=0.03, rough=0.0):
    """A throat: a buzzing source with an unsteady pitch pushed through a few resonances, plus breath."""
    n = int(SR * dur)
    f = np.full(n, float(freq)) if np.isscalar(freq) else freq
    wobble = lowpass(rng.standard_normal(n), 14) * 14
    src = saw(f * (1 + jitter * wobble / (np.max(np.abs(wobble)) + 1e-9)), dur)
    if rough:
        # a growl: the source is chopped by a slow irregular flutter
        flutter = 0.5 + 0.5 * np.sign(tone(f * 0.5 * (1 + 0.1 * lowpass(rng.standard_normal(n), 6)), dur))
        src = src * (1 - rough + rough * flutter)
    src = src + breath * rng.standard_normal(n) * 0.6
    out = np.zeros(n)
    for centre, width, gain in formants:
        out += gain * bandpass(src, centre - width, centre + width)
    return out


def clang(freq, dur, partials=((1.0, 1.0, 0.25), (2.32, 0.6, 0.16), (3.9, 0.45, 0.1), (5.6, 0.3, 0.07), (7.7, 0.2, 0.05)), strike=0.4):
    """Struck metal: partials that do not line up, each dying at its own rate, and the click of the blow."""
    out = np.zeros(int(SR * dur))
    for mult, amp, tau in partials:
        out += amp * tone(freq * mult, dur, rng.uniform(0, 6)) * decay(dur, tau * dur / 0.5)
    click = highpass(noise(dur), 2500) * decay(dur, 0.006) * strike
    return (out + click) * env(dur, 0.001, 0.02)


def whoosh(dur, lo, hi, peak_at=0.5, width=0.5):
    centre = lo + (hi - lo) * np.exp(-((t(dur) / dur - peak_at) ** 2) / 0.05)
    e = np.exp(-((t(dur) / dur - peak_at) ** 2) / 0.045)
    return moving_bandpass(noise(dur), centre, width) * e


def thump(dur, f0=90, f1=38, tau=0.12):
    return tone(sweep(f0, f1, dur, 0.5), dur) * decay(dur, tau)


def crackle(dur, rate=40, bright=3000):
    out = np.zeros(int(SR * dur))
    for _ in range(int(rate * dur)):
        i = rng.integers(0, len(out) - 200)
        n = rng.integers(40, 200)
        out[i:i + n] += rng.standard_normal(n) * np.exp(-np.arange(n) / (n / 4)) * rng.uniform(0.3, 1.0)
    return highpass(out, bright * 0.3)


def pad_chord(freqs, dur, attack=0.4, release=0.6, bright=1600, detune=0.004, vibrato=0.004):
    """A soft sustained chord: several slightly detuned saws per note, low-passed."""
    out = np.zeros(int(SR * dur))
    for f in freqs:
        for d in (-detune, 0.0, detune):
            vib = 1 + vibrato * np.sin(2 * np.pi * rng.uniform(4.5, 5.5) * t(dur) + rng.uniform(0, 6))
            out += saw(f * (1 + d) * vib, dur)
    return lowpass(out, bright, 2) * env(dur, attack, release) / (3 * len(freqs))


def choir(freqs, dur, attack=0.4, release=0.6, vowel=((600, 120, 1.0), (1040, 140, 0.5), (2250, 200, 0.2))):
    out = np.zeros(int(SR * dur))
    for f in freqs:
        for d in (-0.006, 0.005):
            out += voice(f * (1 + d), dur, vowel, breath=0.08, jitter=0.012)
    return out * env(dur, attack, release) / (2 * len(freqs))


def bell(freq, dur, bright=1.0):
    parts = ((0.5, 0.45, 1.6), (1.0, 1.0, 1.2), (1.19, 0.55, 0.9), (1.5, 0.35, 0.7), (2.0, 0.5 * bright, 0.6), (2.51, 0.3 * bright, 0.4),
             (3.01, 0.22 * bright, 0.3), (4.2, 0.12 * bright, 0.2))
    out = np.zeros(int(SR * dur))
    for mult, amp, tau in parts:
        out += amp * tone(freq * mult * (1 + 0.0015 * np.sin(2 * np.pi * 0.7 * t(dur))), dur, rng.uniform(0, 6)) * decay(dur, tau * dur / 2.5)
    return out * env(dur, 0.003, 0.1) + highpass(noise(dur), 1500) * decay(dur, 0.01) * 0.3


def chime(freq, dur, bright=1.0):
    out = np.zeros(int(SR * dur))
    for mult, amp, tau in ((1.0, 1.0, 0.22), (2.76, 0.5 * bright, 0.13), (5.4, 0.3 * bright, 0.08), (8.93, 0.15 * bright, 0.05)):
        out += amp * tone(freq * mult, dur, rng.uniform(0, 6)) * decay(dur, tau * dur / 0.6)
    return out * env(dur, 0.002, 0.05)


S = {}

# ============================================================== the withered: dry throats, no words left
RASP = ((520, 110, 1.0), (1150, 160, 0.7), (2500, 300, 0.35))


def withered_ambient(k):
    d = 1.5 + 0.2 * k
    f = sweep(96 + 9 * k, 78 + 6 * k, d, 0.7) * (1 + 0.05 * np.sin(2 * np.pi * (1.3 + 0.4 * k) * t(d)))
    v = voice(f, d, RASP, breath=0.9, jitter=0.07, rough=0.75) * env(d, 0.25, 0.7)
    air = moving_bandpass(noise(d), sweep(1400, 700, d), 0.4) * env(d, 0.15, 0.8) * 0.5
    return reverb(v + air, 0.6, 0.22, 0.4)


for k in range(3):
    S[f'withered/ambient{k + 1}'] = withered_ambient(k)
for k in range(2):
    d = 0.42
    f = sweep(150 + 20 * k, 230 + 30 * k, d, 0.4)
    S[f'withered/hurt{k + 1}'] = reverb(voice(f, d, ((700, 150, 1.0), (1500, 200, 0.8), (2800, 300, 0.4)), breath=0.8, jitter=0.08, rough=0.6)
                                        * env(d, 0.01, 0.25), 0.4, 0.2, 0.25)
d = 1.9
S['withered/death'] = reverb(voice(sweep(170, 55, d, 0.6), d, RASP, breath=0.9, jitter=0.09, rough=0.8) * env(d, 0.02, 1.2)
                             + place(d, (1.0, crackle(0.7, 30, 1500) * 0.25)), 0.8, 0.3, 0.6)

# ============================================================== the rust knight: breath in a helmet, plate
def plate(freq, dur, bright=1.0, seed=0):
    """A plate of armour struck or knocked against another: many partials that do not line up, crowded towards the
    top the way a sheet of steel rings, the high ones dying first, and the hard bright crack of metal on metal. (A few
    low pure partials, as in `clang`, sound like a pipe; it is the crowd of high ones that says steel.)"""
    r = np.random.default_rng(1000 + seed)
    out = np.zeros(int(SR * dur))
    for n in range(26):
        mult = 1.0 + 0.62 * n ** 1.45 * (1.0 + r.uniform(-0.06, 0.06))
        f = freq * mult
        if f > 15000:
            break
        amp = (1.0 / (1.0 + 0.11 * n)) * (0.55 + 0.45 * r.random()) * (bright if n > 5 else 1.0)
        tau = dur * (0.42 / (1.0 + 0.16 * n)) * (0.8 + 0.4 * r.random())
        # two close partials beat against each other: the shimmer of a thin sheet
        out += amp * (tone(f, dur, r.uniform(0, 6)) + 0.6 * tone(f * (1.0 + r.uniform(0.002, 0.007)), dur, r.uniform(0, 6))) * decay(dur, tau)
    crack = highpass(noise(dur), 3800) * decay(dur, 0.004) * 1.6 + bandpass(noise(dur), 5200, 9500) * decay(dur, 0.03) * 0.5 * bright
    return (out * 0.5 + crack) * env(dur, 0.0005, 0.02)


def rattle(dur, count, lo, hi, seed=0):
    """Loose plates knocking: a handful of small bright hits."""
    r = np.random.default_rng(2000 + seed)
    parts = [(float(r.uniform(0, dur * 0.7)), plate(float(r.uniform(lo, hi)), float(r.uniform(0.07, 0.14)), 1.2, seed * 31 + i) * float(r.uniform(0.2, 0.5)))
             for i in range(count)]
    return place(dur, *parts)


def knight_ambient(k):
    d = 2.2
    # breath heard through the slits of a helm: hiss, not a note
    breath_in = bandpass(noise(0.9), 900, 3800) * env(0.9, 0.35, 0.3) * 0.5 + lowpass(noise(0.9), 500) * env(0.9, 0.4, 0.3) * 0.3
    breath_out = bandpass(noise(1.1), 700, 3000) * env(1.1, 0.15, 0.75) * 0.6 + lowpass(noise(1.1), 400) * env(1.1, 0.2, 0.7) * 0.35
    x = place(d, (0.0, breath_in * 0.8), (1.0 + 0.05 * k, breath_out))
    # the harness shifts as he breathes
    x = x + place(d, (0.15, rattle(0.6, 3, 900, 1700, k) * 0.5), (1.2, rattle(0.7, 2 + k, 800, 1500, 5 + k) * 0.45),
                  (0.7 + 0.2 * k, plate(620 + 90 * k, 0.5, 0.8, 9 + k) * 0.16))
    return reverb(x, 0.5, 0.22, 0.3, bright=7000)


S['rust_knight/ambient1'] = knight_ambient(0)
S['rust_knight/ambient2'] = knight_ambient(1)
d = 0.7
S['rust_knight/hurt'] = reverb(place(d, (0.0, plate(540, 0.6, 1.2, 20) * 0.9), (0.025, plate(880, 0.4, 1.3, 21) * 0.55), (0.06, rattle(0.4, 3, 1000, 1900, 22) * 0.6),
                                     (0.0, thump(0.12, 110, 60, 0.03) * 0.5))
                               + voice(sweep(120, 90, d), d, ((400, 120, 1.0), (900, 150, 0.4)), breath=0.5, rough=0.5) * env(d, 0.01, 0.5) * 0.3,
                               0.5, 0.2, 0.3, bright=8000)
d = 2.4
S['rust_knight/death'] = reverb(place(d, (0.0, voice(sweep(130, 60, 1.2, 0.7), 1.2, ((400, 120, 1.0), (900, 150, 0.4)), breath=0.6, rough=0.6)
                                          * env(1.2, 0.02, 0.9) * 0.55),
                                      (0.5, plate(420, 0.8, 1.0, 30)), (0.72, plate(680, 0.6, 1.2, 31) * 0.7), (0.95, plate(300, 1.0, 1.0, 32) * 1.0),
                                      (1.0, rattle(0.9, 7, 800, 2000, 33) * 0.8), (1.25, plate(760, 0.5, 1.3, 34) * 0.5),
                                      (0.95, thump(0.5, 80, 35, 0.14) * 1.3), (1.5, plate(520, 0.7, 1.0, 35) * 0.4), (1.7, rattle(0.5, 3, 1100, 2100, 36) * 0.4)),
                                0.9, 0.28, 0.5, bright=8000)
for k in range(2):
    d = 0.34
    S[f'rust_knight/step{k + 1}'] = place(d, (0.0, thump(0.16, 75, 45, 0.045) * 0.9), (0.0, plate(900 + 160 * k, 0.2, 1.2, 40 + k) * 0.42),
                                          (0.035, plate(1500 - 140 * k, 0.13, 1.4, 42 + k) * 0.3), (0.07, rattle(0.22, 2, 1200, 2200, 44 + k) * 0.45))
d = 0.55
S['player/weapon_swing_heavy'] = whoosh(d, 250, 1300, 0.5, 0.5) + whoosh(d, 80, 300, 0.5, 0.6) * 0.8

# ============================================================== the village: a dog's throat, the bell of the watch, the gravedigger
DOG = ((420, 120, 1.0), (980, 180, 0.8), (2100, 300, 0.4))


def hound_ambient(k):
    d = 1.1 + 0.3 * k
    f = sweep(74 + 8 * k, 62, d, 0.8) * (1 + 0.06 * np.sin(2 * np.pi * (5.0 + k) * t(d)))
    growl = voice(f, d, DOG, breath=0.5, jitter=0.1, rough=0.95) * env(d, 0.2, 0.5)
    pant = moving_bandpass(noise(d), sweep(1800, 1100, d), 0.5) * (0.5 + 0.5 * np.sin(2 * np.pi * 6 * t(d))) * env(d, 0.3, 0.4) * 0.25
    return reverb(growl + pant, 0.4, 0.18, 0.3)


S['hound/ambient1'] = hound_ambient(0)
S['hound/ambient2'] = hound_ambient(1)
d = 0.34
yelp_f = np.concatenate([sweep(520, 980, 0.09, 0.5), sweep(980, 430, 0.25, 0.7)])
S['hound/hurt'] = reverb(voice(yelp_f, d, ((800, 200, 1.0), (1700, 260, 0.9), (3000, 400, 0.4)), breath=0.3, jitter=0.05, rough=0.2) * env(d, 0.005, 0.2), 0.4, 0.2, 0.25)
d = 1.3
whine_f = np.concatenate([sweep(640, 820, 0.2, 0.6), sweep(820, 210, 1.1, 0.8)])
S['hound/death'] = reverb(voice(whine_f, d, ((700, 200, 1.0), (1500, 260, 0.7), (2700, 400, 0.3)), breath=0.5, jitter=0.08, rough=0.35) * env(d, 0.01, 0.9)
                          + place(d, (0.85, thump(0.3, 80, 45, 0.07) * 0.5)), 0.6, 0.25, 0.4)
d = 0.42
S['hound/bite'] = reverb(place(d, (0.0, voice(sweep(110, 180, 0.2, 0.5), 0.2, DOG, breath=0.6, jitter=0.1, rough=0.9) * env(0.2, 0.01, 0.08) * 0.9),
                               (0.17, highpass(noise(0.05), 1800) * decay(0.05, 0.006) * 1.4), (0.17, tone(sweep(900, 300, 0.06), 0.06) * decay(0.06, 0.012) * 0.6),
                               (0.19, bandpass(noise(0.2), 300, 1200) * decay(0.2, 0.04) * 0.5)), 0.3, 0.15, 0.2)

# the watchman swings a hand bell over his head and shouts what is left of "who goes there"
d = 1.7
hand_bell = np.zeros(int(SR * d))
for i, at in enumerate((0.0, 0.21, 0.4, 0.62, 0.8, 1.02)):
    stroke = bell(1320 * (1.0 if i % 2 else 1.03), 0.7, 1.6) * (1.0 if i % 2 else 0.8)
    a = int(at * SR)
    hand_bell[a:a + len(stroke)] += stroke[:len(hand_bell) - a]
cry_f = np.concatenate([sweep(150, 210, 0.3, 0.6), sweep(210, 190, 0.5), sweep(190, 120, 0.4, 1.3)])
cry = voice(cry_f, 1.2, ((650, 140, 1.0), (1250, 180, 0.8), (2600, 300, 0.4)), breath=0.7, jitter=0.07, rough=0.6) * env(1.2, 0.05, 0.5)
S['watchman/alarm'] = reverb(hand_bell * 0.7 + place(d, (0.25, cry * 0.9)), 0.9, 0.3, 0.6)

# the gravedigger breathes through a sack, and everything he does sounds of wet earth and a spade
SACKED = ((240, 90, 1.0), (520, 120, 0.7), (980, 180, 0.2))


def gravedigger_ambient(k):
    d = 2.6
    f = sweep(70 + 6 * k, 56, d, 0.8) * (1 + 0.03 * np.sin(2 * np.pi * 0.9 * t(d)))
    v = lowpass(voice(f, d, SACKED, breath=0.8, jitter=0.05, rough=0.7), 900) * env(d, 0.7, 1.1)
    breath = lowpass(moving_bandpass(noise(d), sweep(420, 700 if k else 280, d), 0.5), 1200) * env(d, 0.8, 1.0) * 0.7
    return reverb(v + breath, 0.9, 0.3, 0.5)


S['gravedigger/ambient1'] = gravedigger_ambient(0)
S['gravedigger/ambient2'] = gravedigger_ambient(1)
d = 0.7
S['gravedigger/hurt'] = reverb(lowpass(voice(sweep(120, 74, d, 0.5), d, SACKED, breath=0.7, jitter=0.07, rough=0.8), 1100) * env(d, 0.01, 0.45)
                               + place(d, (0.0, thump(0.2, 140, 70, 0.05) * 0.5)), 0.6, 0.25, 0.4)
d = 3.4
S['gravedigger/death'] = reverb(place(d, (0.0, lowpass(voice(sweep(125, 40, 2.4, 0.6), 2.4, SACKED, breath=0.8, jitter=0.08, rough=0.9), 1000) * env(2.4, 0.03, 1.7)),
                                      (1.7, clang(310, 0.9, strike=0.7) * 0.5), (1.85, clang(1450, 0.35) * 0.2),
                                      (2.1, thump(0.9, 85, 30, 0.22) * 1.6), (2.1, bandpass(noise(0.5), 150, 700) * decay(0.5, 0.09) * 1.0),
                                      (2.2, crackle(0.9, 40, 900) * 0.4)), 1.2, 0.35, 0.8)
d = 0.9
S['gravedigger/dig'] = reverb(place(d, (0.0, thump(0.5, 110, 42, 0.12) * 1.5), (0.0, clang(420, 0.5, strike=1.0) * 0.35),
                                    (0.02, bandpass(noise(0.5), 200, 1400) * decay(0.5, 0.07) * 1.1), (0.12, crackle(0.7, 70, 1100) * 0.6)), 0.6, 0.25, 0.4)
d = 3.0
rumble = lowpass(noise(d), 110, 3) * env(d, 0.6, 1.2) * 3.0 + tone(sweep(38, 52, d), d) * env(d, 0.8, 1.0) * 0.7
moan = choir((sweep(98, 104, d), sweep(146.8, 138.6, d), sweep(73.4, 69.3, d)), d, 0.9, 1.3, ((380, 110, 1.0), (800, 150, 0.5), (2300, 260, 0.12)))
S['gravedigger/raise'] = reverb(rumble + moan * 1.1 + place(d, (0.5, crackle(2.2, 45, 900) * 0.45), (0.9, bell(147, 2.0, 0.6) * 0.35)), 1.6, 0.4, 1.0)

# ============================================================== the quarry: iron on stone, powder, falling rock, and what lives under it
def contour(d, *points):
    """A pitch that passes through (place 0..1, Hz) points over d seconds."""
    xs, fs = zip(*points)
    return np.interp(t(d) / d, xs, fs)


def stone_hit(d, f=900, body=160, grit=0.6):
    """Iron or stone on stone: a hard click, a short ring that is mostly noise, a knock in the floor, and chips."""
    click = bandpass(noise(d), f * 0.6, f * 3.2) * decay(d, 0.018)
    ring = (tone(f, d) + 0.6 * tone(f * 1.83, d) + 0.4 * tone(f * 2.71, d)) * decay(d, 0.05) * 0.35
    low = thump(d, body, body * 0.45, 0.07) * 0.9
    chips = crackle(d, 60, 2600) * decay(d, 0.16) * grit
    return click * 1.3 + ring + low + chips


def grind(d, lo=120, hi=700, seed=0):
    """Stone dragged over stone: noise that sticks and slips."""
    r = np.random.default_rng(900 + seed)
    slip = np.clip(lowpass(r.standard_normal(int(SR * d)), 9) * 9 + 0.55, 0.05, 1.6)
    return bandpass(noise(d), lo, hi) * slip + tone(55, d) * slip * 0.15


for k in (0, 1):
    S[f'quarry/pick{k + 1}'] = reverb(stone_hit(0.5, 1400 - 180 * k, 150, 0.7) + clang(2100 + 240 * k, 0.5, strike=0.8) * 0.22, 0.7, 0.3, 0.5)
d = 1.6
S['quarry/blast'] = reverb(place(d, (0.0, thump(1.4, 130, 26, 0.3) * 2.2), (0.0, lowpass(noise(0.5), 1100) * decay(0.5, 0.1) * 1.8),
                                 (0.0, highpass(noise(0.12), 1500) * decay(0.12, 0.02) * 1.0), (0.18, crackle(1.3, 55, 1400) * decay(1.3, 0.45) * 0.8),
                                 (0.1, lowpass(noise(1.4), 220, 3) * decay(1.4, 0.4) * 2.5)), 1.5, 0.4, 1.0)
d = 0.9
S['quarry/fuse'] = (bandpass(noise(d), 3200, 9000) * (0.5 + 0.5 * np.clip(lowpass(noise(d), 30) * 6, -1, 1)) * 0.7 + crackle(d, 150, 5000) * 0.6) * env(d, 0.03, 0.2)
for k in (0, 1):
    d = 1.3
    S[f'quarry/rockfall{k + 1}'] = reverb(place(d, (0.0, stone_hit(0.6, 420 + 60 * k, 110, 1.0) * 1.3), (0.13 + 0.05 * k, stone_hit(0.5, 610, 140, 0.8) * 0.9),
                                               (0.31, stone_hit(0.5, 350, 90, 0.9) * 0.7), (0.5 - 0.1 * k, stone_hit(0.4, 800, 170, 0.6) * 0.45),
                                               (0.0, lowpass(noise(1.2), 180, 3) * decay(1.2, 0.3) * 2.0), (0.2, crackle(1.0, 50, 1500) * decay(1.0, 0.3) * 0.5)),
                                          1.2, 0.35, 0.8)
d = 0.5
S['quarry/barrow'] = reverb(rattle(d, 7, 260, 900, seed=41) * 0.9 + tone(sweep(1750, 1480, d), d) * env(d, 0.05, 0.3) * 0.12
                            + place(d, (0.05, thump(0.25, 120, 60, 0.05) * 0.6), (0.27, thump(0.2, 105, 55, 0.05) * 0.5)), 0.4, 0.2, 0.25)

# the crawler has no eyes: it clicks to itself in the dark and listens to what comes back
THIN = ((900, 220, 1.0), (2100, 320, 0.9), (3600, 500, 0.5))


def crawler_ambient(k):
    d = 1.5
    out = np.zeros(int(SR * d))
    at, gap = 0.05, 0.2 - 0.03 * k
    while at < d - 0.3:
        c = bandpass(noise(0.03), 1800 + 500 * k, 5200) * decay(0.03, 0.004)
        i = int(at * SR)
        out[i:i + len(c)] += c * (0.6 + 0.4 * rng.random())
        at += gap
        gap = max(0.035, gap * 0.82)
    wheeze = voice(contour(d, (0, 310), (0.6, 280), (1, 340 + 60 * k)), d, THIN, breath=1.4, jitter=0.08, rough=0.3) * env(d, 0.5, 0.5) * 0.12
    return reverb(out + wheeze, 1.2, 0.35, 0.8)


S['crawler/ambient1'] = crawler_ambient(0)
S['crawler/ambient2'] = crawler_ambient(1)
d = 0.4
S['crawler/hurt'] = reverb(voice(contour(d, (0, 560), (0.3, 640), (1, 380)), d, THIN, breath=1.0, jitter=0.08, rough=0.5) * env(d, 0.005, 0.25)
                           + bandpass(noise(d), 2500, 7000) * decay(d, 0.1) * 0.35, 0.6, 0.25, 0.4)
d = 1.3
S['crawler/death'] = reverb(voice(contour(d, (0, 480), (0.25, 420), (1, 110)), d, THIN, breath=1.2, jitter=0.1, rough=0.8) * env(d, 0.01, 0.8)
                            + place(d, (0.5, bandpass(noise(0.03), 1800, 5200) * decay(0.03, 0.004)), (0.78, bandpass(noise(0.03), 1800, 5200) * decay(0.03, 0.004) * 0.7),
                                    (1.12, bandpass(noise(0.03), 1800, 5200) * decay(0.03, 0.004) * 0.4)), 1.0, 0.3, 0.7)
d = 0.95
S['crawler/screech'] = reverb(voice(contour(d, (0, 700), (0.22, 1180), (0.6, 1090), (1, 820)), d, ((1300, 300, 1.0), (2700, 400, 1.0), (4300, 600, 0.6)),
                                    breath=0.8, jitter=0.1, rough=0.85) * env(d, 0.02, 0.35) + bandpass(noise(d), 3000, 6500) * env(d, 0.05, 0.5) * 0.25,
                              1.6, 0.4, 1.1)

# the unhewn is stone and sounds of nothing else
for k in (0, 1):
    d = 1.5
    S[f'unhewn/ambient{k + 1}'] = reverb(grind(d, 110 + 30 * k, 640 + 120 * k, seed=k) * env(d, 0.3, 0.6) * 1.2, 1.0, 0.3, 0.6)
S['unhewn/hurt'] = reverb(stone_hit(0.45, 720, 120, 1.0), 0.6, 0.25, 0.4)
d = 2.4
S['unhewn/death'] = reverb(place(d, (0.0, stone_hit(0.6, 520, 100, 1.0) * 1.2), (0.0, grind(1.4, 100, 600, seed=5) * env(1.4, 0.05, 0.9) * 1.1),
                                 (0.35, stone_hit(0.5, 400, 90, 1.0)), (0.7, stone_hit(0.5, 640, 130, 0.9) * 0.8), (1.0, thump(0.9, 80, 30, 0.2) * 1.8),
                                 (1.0, crackle(1.3, 45, 1300) * decay(1.3, 0.4) * 0.8), (1.45, stone_hit(0.4, 880, 150, 0.6) * 0.4)), 1.3, 0.35, 0.9)
d = 1.8
S['unhewn/wake'] = reverb(place(d, (0.0, highpass(noise(0.08), 1800) * decay(0.08, 0.012) * 1.6), (0.0, stone_hit(0.5, 1100, 140, 0.9)),
                                (0.25, grind(1.5, 90, 520, seed=7) * env(1.5, 0.6, 0.4) * 1.3), (0.9, stone_hit(0.4, 760, 120, 0.8) * 0.6),
                                (0.2, crackle(1.4, 30, 1500) * 0.35)), 1.2, 0.35, 0.8)
d = 1.2
S['unhewn/slam'] = reverb(place(d, (0.0, thump(1.0, 85, 28, 0.2) * 2.2), (0.0, stone_hit(0.6, 380, 90, 1.0) * 1.2),
                                (0.0, lowpass(noise(0.4), 600) * decay(0.4, 0.07) * 1.2), (0.12, crackle(1.0, 60, 1400) * decay(1.0, 0.3) * 0.7)), 1.1, 0.35, 0.8)
d = 0.42
S['unhewn/step'] = reverb(grind(d, 140, 620, seed=9) * env(d, 0.02, 0.22) * 0.8 + place(d, (0.2, thump(0.2, 95, 45, 0.05) * 1.0)), 0.5, 0.2, 0.3)

# the taskmaster breathes through a cloth, and his work is a maul, wedges and the rock they split
CLOTHED = ((280, 100, 1.0), (640, 140, 0.6), (1150, 200, 0.18))


def taskmaster_ambient(k):
    d = 2.4
    f = contour(d, (0, 86 + 6 * k), (0.5, 74), (1, 66)) * (1 + 0.03 * np.sin(2 * np.pi * 1.1 * t(d)))
    v = lowpass(voice(f, d, CLOTHED, breath=0.7, jitter=0.05, rough=0.75), 800) * env(d, 0.5, 1.0)
    breath = lowpass(moving_bandpass(noise(d), sweep(360, 760 if k else 300, d), 0.5), 1000) * env(d, 0.6, 0.9) * 0.9
    return reverb(v + breath, 1.0, 0.3, 0.6)


S['taskmaster/ambient1'] = taskmaster_ambient(0)
S['taskmaster/ambient2'] = taskmaster_ambient(1)
d = 0.65
S['taskmaster/hurt'] = reverb(lowpass(voice(contour(d, (0, 140), (0.2, 150), (1, 84)), d, CLOTHED, breath=0.6, jitter=0.07, rough=0.85), 1000) * env(d, 0.01, 0.4), 0.7, 0.25, 0.5)
d = 3.6
S['taskmaster/death'] = reverb(place(d, (0.0, lowpass(voice(contour(2.5, (0, 150), (0.15, 130), (1, 44)), 2.5, CLOTHED, breath=0.8, jitter=0.08, rough=0.9), 900) * env(2.5, 0.03, 1.8)),
                                     (1.9, clang(170, 1.2, strike=0.9) * 0.7), (1.9, stone_hit(0.5, 420, 100, 0.8) * 0.8), (2.4, thump(1.0, 80, 28, 0.25) * 1.8),
                                     (2.4, lowpass(noise(0.5), 500) * decay(0.5, 0.09) * 1.0), (2.55, crackle(0.9, 40, 1000) * 0.4)), 1.4, 0.35, 1.0)
d = 1.9
S['taskmaster/shout'] = reverb(voice(contour(d, (0, 105), (0.2, 168), (0.65, 150), (1, 88)), d, ((420, 130, 1.0), (980, 180, 0.9), (2300, 300, 0.35)),
                                     breath=0.5, jitter=0.07, rough=0.9) * env(d, 0.06, 0.6), 1.8, 0.4, 1.3)
d = 1.1
S['taskmaster/maul'] = reverb(place(d, (0.0, thump(0.9, 96, 30, 0.18) * 2.2), (0.0, clang(180, 0.9, strike=1.0) * 0.5), (0.0, stone_hit(0.5, 520, 110, 1.0) * 1.0),
                                    (0.1, crackle(0.9, 60, 1400) * decay(0.9, 0.25) * 0.6)), 1.0, 0.35, 0.8)
d = 0.6
S['taskmaster/wedge'] = reverb(clang(2300, d, strike=1.0) * 0.8 + clang(3450, d) * 0.35 + place(d, (0.0, thump(0.15, 220, 120, 0.03) * 0.5)), 0.8, 0.3, 0.6)
d = 1.5
cracks = np.zeros(int(SR * d))
at, gap = 0.0, 0.2
while at < d - 0.25:
    c = highpass(noise(0.07), 1400) * decay(0.07, 0.011) * (0.7 + 0.5 * rng.random()) + thump(0.07, 260, 110, 0.02) * 0.6
    i = int(at * SR)
    cracks[i:i + len(c)] += c[:len(cracks) - i]
    at += gap
    gap = max(0.035, gap * 0.8)
S['taskmaster/crack'] = reverb(cracks + lowpass(noise(d), 200, 3) * env(d, 0.3, 0.6) * 2.2 + crackle(d, 50, 1600) * env(d, 0.4, 0.5) * 0.5, 1.2, 0.35, 0.9)

# the stone-eater: a chest like a cellar, and rock giving way round it
DEEP = ((170, 80, 1.0), (430, 120, 0.9), (900, 200, 0.4))


def eater_ambient(k):
    d = 2.8
    f = contour(d, (0, 50 + 4 * k), (0.5, 43), (1, 38 + 6 * k)) * (1 + 0.04 * np.sin(2 * np.pi * 0.7 * t(d)))
    v = voice(f, d, DEEP, breath=0.4, jitter=0.06, rough=1.0) * env(d, 0.6, 1.1)
    gravel = lowpass(noise(d), 320) * (0.5 + 0.5 * np.clip(lowpass(noise(d), 12) * 8, -1, 1)) * env(d, 0.7, 1.0) * 0.8
    return reverb(v * 1.2 + gravel, 1.4, 0.35, 0.9)


S['stone_eater/ambient1'] = eater_ambient(0)
S['stone_eater/ambient2'] = eater_ambient(1)
d = 0.9
S['stone_eater/hurt'] = reverb(voice(contour(d, (0, 98), (0.2, 112), (1, 58)), d, DEEP, breath=0.5, jitter=0.08, rough=1.0) * env(d, 0.01, 0.55)
                               + bandpass(noise(d), 900, 2600) * decay(d, 0.15) * 0.25, 1.0, 0.3, 0.7)
d = 3.8
S['stone_eater/death'] = reverb(place(d, (0.0, voice(contour(2.8, (0, 105), (0.2, 90), (1, 30)), 2.8, DEEP, breath=0.6, jitter=0.09, rough=1.0) * env(2.8, 0.03, 2.0) * 1.2),
                                      (0.0, lowpass(noise(3.4), 160, 3) * env(3.4, 0.5, 1.5) * 2.0), (2.2, thump(1.2, 70, 24, 0.3) * 2.2),
                                      (2.2, stone_hit(0.6, 380, 90, 1.0)), (2.4, crackle(1.3, 45, 1200) * decay(1.3, 0.4) * 0.7)), 1.8, 0.4, 1.2)
d = 2.5
S['stone_eater/roar'] = reverb(voice(contour(d, (0, 58), (0.18, 112), (0.6, 96), (1, 46)), d, ((230, 100, 1.0), (560, 150, 1.0), (1200, 260, 0.6), (2400, 400, 0.25)),
                                     breath=0.5, jitter=0.08, rough=1.0) * env(d, 0.08, 0.9) * 1.3
                               + bandpass(noise(d), 1000, 3000) * env(d, 0.2, 1.0) * 0.2 + lowpass(noise(d), 120, 3) * env(d, 0.3, 1.0) * 2.0, 2.0, 0.4, 1.5)
d = 1.6
scrapes = np.zeros(int(SR * d))
for i, at in enumerate((0.0, 0.34, 0.66, 0.98)):
    sc = bandpass(noise(0.3), 220, 1500 - 150 * i) * env(0.3, 0.12, 0.1) * (1.0 - 0.1 * i) + stone_hit(0.3, 520, 110, 0.9) * 0.4
    a = int(at * SR)
    scrapes[a:a + len(sc)] += sc
S['stone_eater/burrow'] = reverb(scrapes + lowpass(noise(d), 150, 3) * env(d, 0.2, 0.5) * 2.4 + crackle(d, 35, 1300) * 0.35, 1.1, 0.35, 0.8)
d = 1.8
S['stone_eater/erupt'] = reverb(place(d, (0.0, thump(1.5, 110, 24, 0.33) * 2.4), (0.0, lowpass(noise(0.6), 900) * decay(0.6, 0.12) * 1.8),
                                      (0.02, stone_hit(0.6, 420, 100, 1.0) * 1.1), (0.15, stone_hit(0.5, 640, 130, 0.9) * 0.7),
                                      (0.1, crackle(1.6, 70, 1300) * decay(1.6, 0.5) * 0.9), (0.0, lowpass(noise(1.6), 200, 3) * decay(1.6, 0.45) * 2.4)), 1.6, 0.4, 1.1)
d = 0.55
S['stone_eater/bite'] = reverb(place(d, (0.0, stone_hit(0.3, 900, 170, 0.5) * 1.0), (0.035, stone_hit(0.3, 700, 140, 0.4) * 0.8),
                                     (0.0, voice(contour(0.4, (0, 90), (1, 60)), 0.4, DEEP, breath=0.5, jitter=0.08, rough=1.0) * env(0.4, 0.01, 0.25) * 0.8)), 0.7, 0.3, 0.5)

# ============================================================== the overseer: a huge chest, iron, a chain
BIG = ((260, 90, 1.0), (620, 130, 0.8), (1300, 220, 0.35))


def overseer_ambient(k):
    d = 2.4
    f = sweep(62 + 5 * k, 50, d, 0.8) * (1 + 0.04 * np.sin(2 * np.pi * 1.1 * t(d)))
    v = voice(f, d, BIG, breath=0.7, jitter=0.05, rough=0.85) * env(d, 0.5, 1.0)
    wheeze = moving_bandpass(noise(d), sweep(500, 900 if k else 300, d), 0.4) * env(d, 0.6, 0.9) * 0.4
    return reverb(v + wheeze, 1.0, 0.3, 0.6)


S['overseer/ambient1'] = overseer_ambient(0)
S['overseer/ambient2'] = overseer_ambient(1)
d = 0.8
S['overseer/hurt'] = reverb(voice(sweep(95, 60, d, 0.5), d, BIG, breath=0.6, jitter=0.06, rough=0.8) * env(d, 0.015, 0.5), 0.7, 0.25, 0.4)
d = 3.2
S['overseer/death'] = reverb(place(d, (0.0, voice(sweep(110, 34, 2.4, 0.6), 2.4, BIG, breath=0.7, jitter=0.07, rough=0.9) * env(2.4, 0.03, 1.6)),
                                   (1.9, thump(0.9, 70, 28, 0.25) * 1.6), (1.9, lowpass(noise(0.8), 500) * decay(0.8, 0.2) * 0.8),
                                   (2.0, clang(150, 1.0) * 0.5), (2.2, clang(900, 0.3) * 0.2), (2.35, clang(1200, 0.25) * 0.15)), 1.2, 0.35, 0.8)
d = 2.6
roar_f = np.concatenate([sweep(70, 118, 0.5, 0.6), sweep(118, 104, 1.5), sweep(104, 58, 0.6, 1.4)])
roar = voice(roar_f, d, ((300, 110, 1.0), (750, 180, 0.9), (1500, 260, 0.5), (2600, 400, 0.2)), breath=0.8, jitter=0.08, rough=0.9)
roar = np.tanh(roar / (np.max(np.abs(roar)) + 1e-9) * 3.0)
S['overseer/roar'] = reverb(roar * env(d, 0.12, 0.7) + tone(sweep(45, 60, d), d) * env(d, 0.3, 0.8) * 0.4, 1.4, 0.35, 1.0)
d = 1.3
S['overseer/slam'] = reverb(place(d, (0.0, thump(0.9, 95, 30, 0.22) * 1.8), (0.0, lowpass(noise(0.6), 900) * decay(0.6, 0.1) * 1.0),
                                  (0.0, clang(120, 1.0, strike=0.8) * 0.6), (0.05, crackle(0.8, 50, 1200) * 0.4)), 1.0, 0.3, 0.6)
d = 0.7
S['overseer/swing'] = whoosh(d, 120, 700, 0.55, 0.55) * 1.2 + whoosh(d, 60, 200, 0.55, 0.6)
d = 1.1
chain = np.zeros(int(SR * d))
for i in range(22):
    at = rng.uniform(0, d - 0.15) * (0.5 + 0.5 * i / 22)
    c = clang(rng.uniform(1400, 3200), 0.1, strike=0.6) * rng.uniform(0.3, 1.0)
    j = int(at * SR)
    chain[j:j + len(c)] += c[:len(chain) - j]
S['overseer/chain'] = reverb(chain * 0.6 + whoosh(d, 300, 1500, 0.45, 0.5) * 0.7, 0.5, 0.2, 0.3)

# ============================================================== the chaplain: a service nobody listens to
def chaplain_ambient(k):
    d = 3.0
    notes = ((146.8, 174.6), (138.6, 164.8))[k]            # d and f, then c sharp and e: a step down
    x = choir(notes, d, 0.9, 1.2, ((450, 110, 1.0), (800, 130, 0.6), (2400, 220, 0.15)))
    murmur = voice(sweep(110, 104, d) * (1 + 0.03 * np.sign(np.sin(2 * np.pi * 2.6 * t(d)))), d, ((500, 120, 1.0), (1100, 150, 0.5)),
                   breath=0.4, jitter=0.03) * env(d, 0.6, 1.0) * 0.5 * (0.6 + 0.4 * np.abs(np.sin(2 * np.pi * 2.6 * t(d))))
    return reverb(x + murmur, 1.6, 0.45, 1.0)


S['chaplain/ambient1'] = chaplain_ambient(0)
S['chaplain/ambient2'] = chaplain_ambient(1)
d = 0.6
S['chaplain/hurt'] = reverb(moving_bandpass(noise(d), sweep(3200, 1100, d, 0.5), 0.35) * env(d, 0.005, 0.4)
                            + voice(sweep(330, 190, d, 0.5), d, ((900, 180, 1.0), (2100, 260, 0.6)), breath=0.6, jitter=0.06) * env(d, 0.01, 0.4) * 0.6,
                            0.7, 0.3, 0.4)
d = 3.0
S['chaplain/death'] = reverb(choir((sweep(293.7, 110, d, 1.5), sweep(349.2, 98, d, 1.6), sweep(220.0, 73.4, d, 1.4)), d, 0.05, 2.0) * 1.2
                             + place(d, (0.0, bell(110, 2.8) * 0.5), (1.6, crackle(1.2, 25, 1200) * 0.3)), 1.6, 0.45, 1.0)
d = 0.55
S['chaplain/cast'] = reverb(tone(sweep(700, 1900, d, 0.6), d) * env(d, 0.01, 0.4) * 0.5 + chime(1568, d) * 0.6
                            + moving_bandpass(noise(d), sweep(2000, 6000, d), 0.3) * env(d, 0.02, 0.3) * 0.4, 0.6, 0.3, 0.4)
S['chaplain/bell'] = reverb(bell(98, 3.2) + place(3.2, (0.0, thump(0.4, 110, 60, 0.08) * 0.6)), 1.6, 0.35, 0.8)
d = 0.7
S['chaplain/blink'] = reverb(place(d, (0.0, whoosh(0.5, 400, 3500, 0.85, 0.4)), (0.42, thump(0.25, 220, 80, 0.05) * 0.9),
                                   (0.42, chime(1175, 0.28) * 0.4)), 0.6, 0.3, 0.4)

# ============================================================== the keykeeper
def cough(k):
    d = 1.4
    x = np.zeros(int(SR * d))
    for at, strength in ((0.0, 1.0), (0.28, 0.7), (0.52, 0.45)):
        c = bandpass(noise(0.2), 250, 1900) * decay(0.2, 0.035) + voice(sweep(140 - 10 * k, 95, 0.2), 0.2, ((500, 150, 1.0), (1300, 200, 0.5)),
                                                                       breath=0.9, rough=0.7) * decay(0.2, 0.06) * 0.8
        j = int(at * SR)
        x[j:j + len(c)] += c * strength
    sigh = moving_bandpass(noise(0.6), sweep(900, 400, 0.6), 0.4) * env(0.6, 0.1, 0.45) * 0.4
    return reverb(place(d, (0.0, x), (0.8, sigh)), 0.5, 0.2, 0.3)


S['keykeeper/ambient1'] = cough(0)
S['keykeeper/ambient2'] = cough(1)
d = 1.0
keys = np.zeros(int(SR * d))
for i in range(14):
    at = rng.uniform(0, 0.7)
    c = clang(rng.uniform(2200, 5200), 0.25, partials=((1.0, 1.0, 0.2), (2.7, 0.5, 0.1), (4.9, 0.3, 0.06)), strike=0.3) * rng.uniform(0.3, 1.0)
    j = int(at * SR)
    keys[j:j + len(c)] += c[:len(keys) - j]
S['keykeeper/give'] = reverb(keys, 0.4, 0.2, 0.3)

# ============================================================== the game itself: death, victory, hearths, souls
D2, F2, A2, D3, F3, A3, D4, F4, A4, C4, E4, G3, BB2, G2, FS3, FS4 = 73.42, 87.31, 110.0, 146.83, 174.61, 220.0, 293.66, 349.23, 440.0, 261.63, 329.63, 196.0, 116.54, 98.0, 185.0, 369.99
d = 3.8
S['ui/death'] = reverb(pad_chord((D2, A2, D3, F3), d, 0.02, 2.6, 900) * 1.4 + tone(D2 / 2, d) * decay(d, 1.2) * 0.9
                       + place(d, (0.0, bell(D3, 3.0) * 0.25), (0.0, thump(0.8, 70, 30, 0.3) * 1.2)), 1.8, 0.4, 1.0, 3000)
d = 4.6
S['ui/boss_defeated'] = reverb(place(d, (0.0, pad_chord((D3, A3, D4, F4), 1.6, 0.4, 0.5, 2200)), (1.3, pad_chord((D3, A3, D4, FS4), 3.2, 0.5, 2.2, 2600) * 1.2),
                                     (0.0, choir((D3, A3), 1.6, 0.5, 0.5) * 0.8), (1.3, choir((D3, FS3, A3), 3.2, 0.5, 2.2) * 0.9),
                                     (1.3, bell(D3, 3.0) * 0.5), (1.3, thump(0.8, 80, 36, 0.3) * 1.0), (0.0, tone(D2, d) * env(d, 0.5, 2.0) * 0.5)),
                               1.8, 0.4, 1.0)
d = 2.6
S['ui/hearth_lit'] = reverb(place(d, (0.0, whoosh(0.9, 200, 1800, 0.3, 0.6) * 1.2), (0.1, lowpass(noise(1.2), 700) * env(1.2, 0.05, 1.0) * 0.6),
                                  (0.25, crackle(2.0, 35, 2500) * 0.35), (0.3, pad_chord((D3, A3), 2.2, 0.5, 1.3, 1800) * 0.9),
                                  (0.5, chime(D4 * 2, 1.4) * 0.3), (0.8, chime(A4 * 2, 1.4) * 0.25)), 1.2, 0.35, 0.8)
d = 3.2
S['ui/place'] = reverb(place(d, (0.0, pad_chord((D3, A3), 2.8, 0.9, 1.6, 1300) * 0.8), (0.0, tone(D2, d) * env(d, 0.8, 1.8) * 0.35),
                             (0.35, bell(D3, 2.4) * 0.3), (0.9, choir((A3,), 2.0, 0.6, 1.2) * 0.35)), 1.6, 0.4, 0.9)
d = 1.5
S['ui/level_up'] = reverb(place(d, (0.0, chime(D4 * 2, 0.9) * 0.5), (0.13, chime(F4 * 2, 0.9) * 0.5), (0.26, chime(A4 * 2, 0.9) * 0.55),
                                (0.39, chime(D4 * 4, 1.0) * 0.6), (0.0, pad_chord((D4, A4), d, 0.2, 0.9, 2400) * 0.5)), 1.0, 0.35, 0.6)


def shimmer(d, lo, hi, count):
    x = np.zeros(int(SR * d))
    for i in range(count):
        f = rng.uniform(lo, hi)
        at = rng.uniform(0, d * 0.55)
        n = d - at
        g = tone(f * (1 + 0.004 * np.sin(2 * np.pi * rng.uniform(3, 7) * t(n))), n) * env(n, n * 0.5, n * 0.45) * rng.uniform(0.3, 1.0)
        j = int(at * SR)
        x[j:j + len(g)] += g[:len(x) - j]
    air = moving_bandpass(noise(d), sweep(lo * 0.8, hi * 1.3, d, 0.7), 0.4) * env(d, d * 0.6, d * 0.3) * 0.6
    return x / count * 3 + air


S['ui/souls_gain'] = reverb(shimmer(0.7, 900, 2600, 10), 0.5, 0.3, 0.3)
S['ui/souls_retrieve'] = reverb(shimmer(1.8, 500, 3000, 22) + place(1.8, (0.9, chime(D4 * 2, 0.8) * 0.3)) + pad_chord((D3, A3), 1.8, 0.8, 0.8, 1500) * 0.5,
                                1.2, 0.4, 0.8)
d = 2.2
S['block/hearth_rest'] = reverb(crackle(d, 30, 2500) * 0.35 + lowpass(noise(d), 500) * env(d, 0.6, 1.2) * 0.4
                                + pad_chord((D3, A3, D4), d, 0.7, 1.2, 1400) * 0.9, 1.0, 0.35, 0.8)
d = 1.3
S['block/fog_gate_pass'] = reverb(whoosh(d, 150, 900, 0.4, 0.6) * 1.2 + lowpass(noise(d), 350) * env(d, 0.3, 0.7) * 0.5
                                  + tone(sweep(180, 120, d), d) * env(d, 0.4, 0.6) * 0.15, 1.2, 0.45, 0.8, 2500)

# ============================================================== items
d = 1.25
gulps = np.zeros(int(SR * d))
for i, at in enumerate((0.05, 0.4, 0.75)):
    g = tone(sweep(170, 420, 0.16, 1.8), 0.16) * env(0.16, 0.01, 0.1) + bandpass(noise(0.16), 300, 900) * env(0.16, 0.02, 0.1) * 0.25
    j = int(at * SR)
    gulps[j:j + len(g)] += g * (1.0 - 0.15 * i)
S['item/flask_drink'] = reverb(gulps + place(d, (0.75, shimmer(0.5, 1200, 2400, 6) * 0.25)), 0.3, 0.15, 0.2)
d = 0.5
S['item/flask_empty'] = place(d, (0.0, tone(sweep(520, 380, 0.12), 0.12) * decay(0.12, 0.03) + bandpass(noise(0.12), 400, 1500) * decay(0.12, 0.012) * 0.6),
                              (0.2, (tone(sweep(480, 350, 0.12), 0.12) * decay(0.12, 0.03) + bandpass(noise(0.12), 400, 1500) * decay(0.12, 0.012) * 0.6) * 0.6))
d = 0.45
S['item/spell_shard'] = reverb(tone(sweep(1500, 420, d, 0.4), d) * env(d, 0.005, 0.3) * 0.6 + chime(1320, d, 1.2) * 0.5
                               + moving_bandpass(noise(d), sweep(5000, 1500, d), 0.3) * env(d, 0.005, 0.3) * 0.35, 0.5, 0.25, 0.3)
d = 0.4
S['item/spell_shard_hit'] = reverb(chime(1760, d, 1.4) * 0.7 + highpass(noise(d), 3000) * decay(d, 0.03) * 0.5
                                   + place(d, (0.0, chime(2637, d * 0.7, 1.2) * 0.4), (0.0, thump(0.15, 200, 90, 0.04) * 0.5)), 0.4, 0.25, 0.3)
d = 1.7
S['item/spell_mend'] = reverb(place(d, (0.0, choir((D4, F4, A4), d, 0.5, 0.9, ((500, 120, 1.0), (900, 140, 0.5), (2600, 220, 0.2))) * 0.9),
                                    (0.2, chime(D4 * 2, 0.9) * 0.3), (0.4, chime(A4 * 2, 0.9) * 0.3), (0.6, chime(D4 * 4, 1.0) * 0.3),
                                    (0.0, shimmer(d, 1500, 4000, 14) * 0.2)), 1.2, 0.4, 0.7)
d = 0.95
S['item/spell_rebuke'] = reverb(place(d, (0.0, thump(0.7, 130, 40, 0.16) * 1.6), (0.0, whoosh(0.8, 200, 2400, 0.15, 0.6) * 1.2),
                                      (0.0, bell(196, 0.9) * 0.25)), 0.9, 0.3, 0.6)
d = 0.65
S['item/spell_fire'] = whoosh(d, 200, 1500, 0.35, 0.6) * 1.2 + lowpass(noise(d), 600) * env(d, 0.05, 0.4) * 0.6 + crackle(d, 60, 2500) * 0.3
d = 1.1
S['item/spell_fire_burst'] = reverb(place(d, (0.0, thump(0.7, 120, 35, 0.15) * 1.6), (0.0, lowpass(noise(0.9), 1400) * decay(0.9, 0.16) * 1.2),
                                          (0.05, crackle(0.9, 70, 2500) * 0.5)), 0.8, 0.25, 0.5)
d = 0.45
S['item/spell_fizzle'] = moving_bandpass(noise(d), sweep(2400, 500, d, 0.6), 0.4) * env(d, 0.01, 0.35) + tone(sweep(420, 160, d), d) * env(d, 0.01, 0.3) * 0.25

# ============================================================== the player's own moves
d = 0.4
S['player/roll'] = place(d, (0.0, moving_bandpass(noise(0.3), sweep(500, 1500, 0.3), 0.6) * env(0.3, 0.05, 0.2) * 0.9),
                         (0.14, thump(0.2, 90, 50, 0.05) * 0.9), (0.16, lowpass(noise(0.2), 700) * decay(0.2, 0.04) * 0.6))
d = 0.45
S['player/backstab'] = place(d, (0.0, highpass(noise(0.12), 2500) * decay(0.12, 0.02) * 1.0), (0.0, tone(sweep(2400, 900, 0.1), 0.1) * decay(0.1, 0.02) * 0.5),
                             (0.03, thump(0.3, 150, 50, 0.07) * 1.4), (0.03, lowpass(noise(0.25), 900) * decay(0.25, 0.05) * 0.7))
d = 0.7
S['player/guard_break'] = reverb(place(d, (0.0, clang(240, 0.6, strike=0.9)), (0.0, lowpass(noise(0.3), 1800) * decay(0.3, 0.05) * 0.9),
                                       (0.02, thump(0.3, 120, 50, 0.08) * 1.0), (0.08, clang(610, 0.4) * 0.4)), 0.5, 0.2, 0.3)

# ============================================================== boss music: 48 seconds that loop, in D minor
def boss_music():
    bpm = 100.0
    beat = 60.0 / bpm
    bars = 20
    total = bars * 4 * beat                 # 48 s
    out = np.zeros(int(SR * total) + SR * 3)

    def put(at, x, gain=1.0):
        j = int(at * SR)
        out[j:j + len(x)] += x[:len(out) - j] * gain
    # the harmony: four bars per chord, the last phrase climbs to the dominant and falls back
    chords = [(D2, (D3, F3, A3)), (BB2, (D3, F3, 233.08)), (G2, (D3, G3, 233.08)), (A2, (C4 * 0 + 277.18, E4, A3)), (D2, (D3, F3, A3))]
    for i, (bass, notes) in enumerate(chords):
        at = i * 4 * 4 * beat
        length = 4 * 4 * beat
        put(at, pad_chord(notes, length + 0.6, 1.2, 1.4, 1500), 0.9)
        put(at, choir(notes[:2], length + 0.6, 1.5, 1.5), 0.45)
        put(at, lowpass(saw(bass, length) + saw(bass * 1.005, length), 220) * env(length, 0.3, 0.8), 0.5)
        put(at, tone(bass / 2, length) * env(length, 0.4, 0.8), 0.5)
        # low strings sawing eighth notes: root, root, fifth, root...
        pattern = (1.0, 1.0, 1.5, 1.0, 1.0, 1.2, 1.5, 1.0)
        for b in range(4 * 4 * 2):
            f = bass * 2 * pattern[b % 8]
            n = beat / 2
            note = lowpass(saw(f, n) + saw(f * 1.006, n), 900) * env(n, 0.012, n * 0.55)
            accent = 1.0 if b % 4 == 0 else 0.6
            put(at + b * n, note, 0.34 * accent)
    # drums: a timpani on one and on the "and" of three, a deep hit every four bars, a rattle of sticks to push it on
    for bar in range(bars):
        at = bar * 4 * beat
        root = chords[bar // 4][0]
        for when, gain in ((0.0, 1.0), (2.5, 0.6), (3.0, 0.75), (3.5, 0.5)):
            hit = tone(sweep(root * 1.3, root, 0.5, 0.3), 0.5) * decay(0.5, 0.16) + lowpass(noise(0.5), 400) * decay(0.5, 0.03) * 0.5
            put(at + when * beat, hit, 0.85 * gain)
        if bar % 4 == 0:
            put(at, thump(1.2, 60, 28, 0.4), 1.2)
            put(at, bell(D3 if bar % 8 == 0 else A2, 2.6), 0.14)
        for s16 in range(16):
            if s16 % 4 in (2, 3) or bar % 4 == 3:
                tick = highpass(noise(0.05), 5000) * decay(0.05, 0.012)
                put(at + s16 * beat / 4, tick, 0.10 if s16 % 2 else 0.16)
    # a horn call over the second half of every phrase
    melody = [(0, D4, 1.5), (1.5, F4, 0.5), (2, E4, 1), (3, D4, 1), (4, A3, 3), (8, D4, 1.5), (9.5, F4, 0.5), (10, G3 * 2, 1), (11, A4, 1), (12, F4, 3)]
    for phrase in range(5):
        base = (phrase * 16 + 8) * beat
        if phrase == 4:
            base = phrase * 16 * beat
        for at, f, length in melody[:5] if phrase % 2 == 0 else melody[5:]:
            n = length * beat
            src = saw(f * (1 + 0.004 * np.sin(2 * np.pi * 5 * t(n))), n) + 0.5 * saw(f * 0.5, n)
            horn = lowpass(src, 1100, 2) * env(n, 0.07, min(0.25, n * 0.4))
            put(base + (at % 8) * beat, horn, 0.22)
    x = reverb(out[:int(SR * total)], 1.6, 0.3, 0.0, 3500)
    # the tail of the reverb is folded back onto the beginning, so the end runs into the start without a seam
    n = int(SR * total)
    loop = x[:n].copy()
    fade = int(SR * 0.05)
    loop[:fade] *= np.linspace(0, 1, fade)
    loop[-fade:] *= np.linspace(1, 0, fade)
    return loop


# ============================================================== the Mire: water, wet wood, and what the water keeps
def splash(d, size=1.0, bright=5000):
    """Something going into water or coming out of it: the slap, the spray falling back, and the hollow under it."""
    body = bandpass(noise(d), 500, bright) * env(d, 0.004, d * 0.8) * decay(d, d * 0.22)
    spray = highpass(noise(d), 3000) * decay(d, d * 0.35) * 0.35 * np.clip(lowpass(noise(d), 30) * 6 + 0.6, 0, 1)
    low = thump(d, 120 / size, 45 / size, 0.07 * size) * 0.9 * size
    return body + spray + low


def bubbles(d, count, lo=300, hi=1100):
    out = np.zeros(int(SR * d))
    for _ in range(count):
        n = rng.uniform(0.03, 0.09)
        f0 = rng.uniform(lo, hi)
        b = tone(sweep(f0, f0 * rng.uniform(1.25, 1.7), n), n) * env(n, 0.004, n * 0.6) * rng.uniform(0.3, 1.0)
        i = rng.integers(0, max(1, len(out) - len(b)))
        out[i:i + len(b)] += b
    return out


def creak(d, f0, f1, body=(420, 900, 1700)):
    """Wood under strain: a slow stick and slip, every slip ringing the timber."""
    n = int(SR * d)
    wob = lowpass(rng.standard_normal(n), 5)
    rate = contour(d, (0, f0), (0.5, (f0 + f1) * 0.5), (1, f1)) * (1 + 0.22 * wob / (np.max(np.abs(wob)) + 1e-9))
    slips = highpass(saw(rate, d), 220)
    out = np.zeros(n)
    for i, centre in enumerate(body):
        out += bandpass(slips, centre * 0.86, centre * 1.14) / (1 + i * 0.5)
    hold = np.clip(lowpass(rng.standard_normal(n), 7) * 7 + 0.7, 0.1, 1.5)
    return out * hold


def squelch(d, f=260):
    return lowpass(noise(d), f * 3) * env(d, d * 0.3, d * 0.5) * np.clip(lowpass(noise(d), 25) * 5 + 0.5, 0, 1.2) + thump(d, f * 0.5, f * 0.22, d * 0.3) * 0.8


# the bone pike: jaws that are all bone, in water
def pike_ambient(k):
    d = 1.5
    clicks = np.zeros(int(SR * d))
    for i in range(3 + k * 2):
        c = highpass(noise(0.03), 1800) * decay(0.03, 0.004) + tone(1900 + 300 * rng.random(), 0.03) * decay(0.03, 0.006) * 0.5
        a = int((0.15 + 0.11 * i + 0.03 * rng.random()) * SR)
        clicks[a:a + len(c)] += c * (0.6 + 0.4 * rng.random())
    return reverb(clicks * 0.8 + bubbles(d, 9 + 4 * k, 260, 900) * 0.5 + lowpass(noise(d), 300) * env(d, 0.4, 0.6) * 0.5, 0.6, 0.25, 0.4)


S['pike/ambient1'] = pike_ambient(0)
S['pike/ambient2'] = pike_ambient(1)
d = 0.45
S['pike/hurt'] = reverb(voice(contour(d, (0, 520), (0.3, 640), (1, 330)), d, THIN, breath=0.7, jitter=0.1, rough=0.8) * env(d, 0.005, 0.3) * 0.7
                        + place(d, (0.0, splash(0.4, 0.6) * 0.5)), 0.5, 0.25, 0.3)
d = 1.5
S['pike/death'] = reverb(place(d, (0.0, voice(contour(0.7, (0, 560), (1, 190)), 0.7, THIN, breath=0.8, jitter=0.1, rough=0.9) * env(0.7, 0.005, 0.5) * 0.7),
                               (0.25, rattle(0.6, 14, 1400, 3200, 31) * 0.5), (0.5, splash(0.8, 0.9) * 0.8), (0.6, bubbles(0.9, 16, 240, 800) * 0.5)), 0.8, 0.3, 0.5)
d = 0.35
S['pike/bite'] = reverb(place(d, (0.0, stone_hit(0.2, 1500, 260, 0.2) * 0.9), (0.03, stone_hit(0.2, 1150, 220, 0.2) * 0.7), (0.0, splash(0.3, 0.5) * 0.3)), 0.4, 0.2, 0.3)
d = 0.9
S['pike/leap'] = reverb(place(d, (0.0, splash(0.6, 0.8) * 1.0), (0.05, whoosh(0.5, 500, 2600, 0.4) * 0.6), (0.35, bubbles(0.5, 8, 300, 900) * 0.3)), 0.6, 0.25, 0.4)
d = 0.8
S['pike/flop'] = reverb(place(d, (0.0, squelch(0.16, 420) * 1.0), (0.0, lowpass(noise(0.08), 2400) * decay(0.08, 0.012) * 1.0), (0.22, squelch(0.14, 500) * 0.8),
                              (0.22, lowpass(noise(0.07), 2600) * decay(0.07, 0.01) * 0.8), (0.47, squelch(0.14, 380) * 0.7),
                              (0.47, lowpass(noise(0.07), 2200) * decay(0.07, 0.01) * 0.7)), 0.4, 0.2, 0.3)

# the lanky: a chest like a hollow log
LOG = ((210, 80, 1.0), (520, 120, 0.7), (1250, 220, 0.25))


def lanky_ambient(k):
    d = 2.6
    v = voice(contour(d, (0, 62 + 5 * k), (0.4, 54), (1, 47 + 4 * k)), d, LOG, breath=0.7, jitter=0.07, rough=0.6) * env(d, 0.7, 1.0)
    return reverb(v * 1.0 + place(d, (0.2 + 0.5 * k, creak(1.3, 22, 34 + 8 * k) * env(1.3, 0.3, 0.5) * 0.35)) + lowpass(noise(d), 400) * env(d, 0.9, 0.9) * 0.3,
                  1.3, 0.35, 0.8)


S['lanky/ambient1'] = lanky_ambient(0)
S['lanky/ambient2'] = lanky_ambient(1)
d = 0.7
S['lanky/hurt'] = reverb(voice(contour(d, (0, 118), (0.2, 134), (1, 70)), d, LOG, breath=0.6, jitter=0.08, rough=0.8) * env(d, 0.01, 0.45)
                         + place(d, (0.0, creak(0.4, 60, 30) * env(0.4, 0.01, 0.3) * 0.5)), 0.8, 0.3, 0.5)
d = 3.0
S['lanky/death'] = reverb(place(d, (0.0, voice(contour(2.2, (0, 128), (0.15, 110), (1, 38)), 2.2, LOG, breath=0.8, jitter=0.09, rough=0.9) * env(2.2, 0.03, 1.6)),
                                (0.6, creak(1.6, 46, 14) * env(1.6, 0.2, 0.5) * 0.6), (2.0, splash(0.9, 1.2) * 0.9), (2.0, crackle(0.8, 60, 1500) * decay(0.8, 0.2) * 0.6),
                                (2.1, bubbles(0.8, 12, 220, 700) * 0.4)), 1.3, 0.35, 0.9)
d = 1.0
S['lanky/slam'] = reverb(place(d, (0.0, thump(0.8, 100, 34, 0.16) * 2.0), (0.0, splash(0.7, 1.1) * 0.8), (0.0, crackle(0.4, 80, 1800) * decay(0.4, 0.08) * 0.8),
                               (0.1, squelch(0.5, 300) * 0.6)), 0.9, 0.3, 0.7)
d = 0.4
S['lanky/step'] = reverb(place(d, (0.0, squelch(0.3, 340) * 1.0), (0.12, bubbles(0.25, 3, 300, 700) * 0.3)), 0.4, 0.2, 0.3)


# the wisp: a small cold light, and the breath of whoever it was
def wisp_ambient(k):
    d = 2.4
    sigh = voice(contour(d, (0, 430 + 40 * k), (0.5, 400), (1, 340 + 30 * k)), d, ((900, 160, 1.0), (1700, 220, 0.5), (3200, 400, 0.3)), breath=1.2, jitter=0.03) \
        * env(d, 0.9, 1.1) * 0.18
    return reverb(shimmer(d, 1700 + 300 * k, 3400, 6) * 0.5 + sigh, 1.6, 0.45, 1.0)


S['wisp/ambient1'] = wisp_ambient(0)
S['wisp/ambient2'] = wisp_ambient(1)
d = 0.5
S['wisp/hurt'] = reverb(place(d, (0.0, chime(2400, 0.45, 1.3) * 0.8), (0.03, chime(1800, 0.4, 1.0) * 0.5), (0.0, highpass(noise(0.25), 3500) * decay(0.25, 0.05) * 0.4)),
                        0.8, 0.35, 0.5)
d = 1.5
S['wisp/death'] = reverb(place(d, (0.0, chime(2200, 0.9, 1.3) * 0.8), (0.08, chime(1650, 0.9) * 0.7), (0.18, chime(1240, 1.0) * 0.6), (0.3, chime(930, 1.1) * 0.5),
                               (0.0, moving_bandpass(noise(1.2), sweep(3600, 500, 1.2, 0.6), 0.4) * env(1.2, 0.02, 0.9) * 0.5)), 1.4, 0.45, 0.9)
d = 1.2
S['wisp/cast'] = reverb(place(d, (0.0, moving_bandpass(noise(1.0), sweep(600, 3600, 1.0, 1.6), 0.35) * env(1.0, 0.6, 0.08) * 0.8),
                              (0.0, tone(sweep(700, 2100, 1.0, 1.6), 1.0) * env(1.0, 0.7, 0.05) * 0.25), (0.95, chime(2600, 0.25, 1.4) * 0.8),
                              (0.95, highpass(noise(0.2), 2500) * decay(0.2, 0.04) * 0.5)), 0.9, 0.35, 0.6)

# the Wallower: a throat as wide as a cart, half under water
WET = ((190, 90, 1.0), (460, 130, 0.9), (1050, 220, 0.35))


def wallower_ambient(k):
    d = 2.8
    f = contour(d, (0, 44 + 4 * k), (0.25, 52), (1, 36 + 5 * k)) * (1 + 0.06 * np.sin(2 * np.pi * (1.6 + 0.5 * k) * t(d)))
    croak = voice(f, d, WET, breath=0.4, jitter=0.07, rough=1.0) * env(d, 0.4, 1.2)
    return reverb(croak * 1.2 + bubbles(d, 20, 180, 600) * env(d, 0.5, 0.8) * 0.6 + lowpass(noise(d), 260) * env(d, 0.8, 1.0) * 0.6, 1.4, 0.35, 0.9)


S['wallower/ambient1'] = wallower_ambient(0)
S['wallower/ambient2'] = wallower_ambient(1)
d = 0.9
S['wallower/hurt'] = reverb(voice(contour(d, (0, 104), (0.2, 122), (1, 56)), d, WET, breath=0.5, jitter=0.08, rough=1.0) * env(d, 0.01, 0.55)
                            + place(d, (0.0, splash(0.6, 1.2) * 0.5)), 1.0, 0.3, 0.7)
d = 4.0
S['wallower/death'] = reverb(place(d, (0.0, voice(contour(3.0, (0, 112), (0.15, 96), (1, 28)), 3.0, WET, breath=0.6, jitter=0.09, rough=1.0) * env(3.0, 0.03, 2.2) * 1.2),
                                   (0.5, bubbles(3.0, 60, 150, 600) * env(3.0, 0.5, 1.0) * 0.6), (2.3, splash(1.5, 2.0) * 1.3),
                                   (2.4, lowpass(noise(1.4), 180, 3) * decay(1.4, 0.4) * 2.0)), 1.8, 0.4, 1.2)
d = 2.4
S['wallower/roar'] = reverb(voice(contour(d, (0, 52), (0.2, 104), (0.6, 92), (1, 40)), d, ((210, 100, 1.0), (520, 150, 1.0), (1150, 260, 0.6), (2300, 400, 0.2)),
                                  breath=0.5, jitter=0.08, rough=1.0) * env(d, 0.08, 0.9) * 1.3
                            + bubbles(d, 40, 160, 700) * env(d, 0.3, 0.8) * 0.5 + lowpass(noise(d), 130, 3) * env(d, 0.3, 1.0) * 1.8, 2.0, 0.4, 1.5)
d = 0.6
S['wallower/bite'] = reverb(place(d, (0.0, thump(0.4, 130, 44, 0.08) * 1.8), (0.0, lowpass(noise(0.12), 2200) * decay(0.12, 0.02) * 1.4), (0.02, splash(0.5, 0.9) * 0.7),
                                  (0.0, voice(contour(0.4, (0, 84), (1, 56)), 0.4, WET, breath=0.5, jitter=0.08, rough=1.0) * env(0.4, 0.01, 0.25) * 0.6)), 0.7, 0.3, 0.5)
d = 1.2
S['wallower/slap'] = reverb(place(d, (0.0, splash(1.1, 1.8, 4200) * 1.3), (0.0, lowpass(noise(0.1), 1500) * decay(0.1, 0.015) * 1.6), (0.0, thump(0.7, 90, 30, 0.14) * 1.6),
                                  (0.3, bubbles(0.8, 14, 200, 800) * 0.4)), 1.0, 0.3, 0.7)
d = 1.1
S['wallower/spit'] = reverb(place(d, (0.0, voice(contour(0.8, (0, 60), (0.8, 130), (1, 150)), 0.8, WET, breath=0.6, jitter=0.1, rough=1.0) * env(0.8, 0.25, 0.05) * 0.9),
                                  (0.0, bubbles(0.8, 26, 200, 900) * env(0.8, 0.4, 0.1) * 0.6), (0.8, lowpass(noise(0.2), 1800) * decay(0.2, 0.035) * 1.5),
                                  (0.8, thump(0.25, 190, 70, 0.05) * 1.0)), 0.8, 0.3, 0.5)
d = 0.6
S['wallower/plop'] = reverb(place(d, (0.0, thump(0.4, 150, 55, 0.07) * 1.4), (0.0, squelch(0.45, 380) * 1.0), (0.03, splash(0.4, 0.7, 3000) * 0.5),
                                  (0.2, bubbles(0.35, 6, 220, 700) * 0.4)), 0.5, 0.25, 0.4)
d = 1.8
S['wallower/dive'] = reverb(place(d, (0.0, splash(1.2, 2.0) * 1.2), (0.0, whoosh(0.9, 200, 1400, 0.3) * 0.7), (0.4, bubbles(1.4, 50, 160, 700) * env(1.4, 0.1, 1.0) * 0.8),
                                  (0.2, lowpass(noise(1.5), 200, 3) * decay(1.5, 0.4) * 1.8)), 1.2, 0.35, 0.9)
d = 1.0
S['wallower/wake'] = reverb(moving_bandpass(noise(d), contour(d, (0, 500), (0.5, 1300), (1, 600)), 0.5) * env(d, 0.4, 0.5) * 0.7
                            + bubbles(d, 12, 240, 800) * env(d, 0.3, 0.4) * 0.5 + lowpass(noise(d), 180) * env(d, 0.4, 0.5) * 0.8, 0.7, 0.25, 0.5)
d = 2.2
S['wallower/breach'] = reverb(place(d, (0.0, splash(1.8, 2.6) * 1.6), (0.0, thump(1.4, 100, 24, 0.3) * 2.2), (0.0, lowpass(noise(0.5), 1100) * decay(0.5, 0.1) * 1.6),
                                    (0.05, voice(contour(1.3, (0, 70), (0.2, 124), (1, 60)), 1.3, WET, breath=0.5, jitter=0.08, rough=1.0) * env(1.3, 0.04, 0.8) * 1.1),
                                    (0.5, highpass(noise(1.5), 2600) * env(1.5, 0.3, 1.1) * 0.3)), 1.6, 0.4, 1.1)
d = 3.4
S['wallower/gulp'] = reverb(place(d, (0.0, moving_bandpass(noise(2.65), sweep(180, 1500, 2.65, 1.8), 0.5) * env(2.65, 1.9, 0.06) * 1.3),
                                  (0.0, voice(contour(2.65, (0, 46), (1, 96)), 2.65, WET, breath=0.9, jitter=0.06, rough=0.7) * env(2.65, 1.2, 0.05) * 0.6),
                                  (0.3, bubbles(2.3, 40, 180, 800) * env(2.3, 1.2, 0.3) * 0.4), (2.65, thump(0.5, 130, 44, 0.09) * 2.0),
                                  (2.65, lowpass(noise(0.12), 2200) * decay(0.12, 0.02) * 1.5), (2.652, splash(0.6, 1.0) * 0.6)), 1.2, 0.35, 0.8)

# the Weeping Mother: a woman's voice out of a tree, and the tree
WOMAN = ((760, 130, 1.0), (1280, 170, 0.6), (2850, 320, 0.3))


def mother_ambient(k):
    """Weeping: three or four short sobs going down, and the long shaken breath after them; the bole creaks under it."""
    d = 3.4
    out = np.zeros(int(SR * d))
    at, pitch = 0.15, 300 - 20 * k
    for i in range(3 + k):
        n = 0.2 + 0.03 * i
        s = voice(contour(n, (0, pitch), (0.3, pitch * 1.08), (1, pitch * 0.82)), n, WOMAN, breath=0.5, jitter=0.05) * env(n, 0.03, n * 0.6)
        a = int(at * SR)
        out[a:a + len(s)] += s * (0.9 - 0.12 * i)
        at += n + 0.09
        pitch *= 0.93
    n = d - at - 0.1
    wob = 1 + 0.05 * np.sin(2 * np.pi * 6.5 * t(n))
    breath = voice(contour(n, (0, pitch), (1, pitch * 0.72)) * wob, n, WOMAN, breath=1.1, jitter=0.06) * env(n, 0.2, n * 0.7) * (0.75 + 0.25 * np.sin(2 * np.pi * 6.5 * t(n)))
    a = int(at * SR)
    out[a:a + len(breath)] += breath * 0.6
    return reverb(out + place(d, (0.6 + 0.4 * k, creak(1.8, 16, 26) * env(1.8, 0.5, 0.7) * 0.22)), 2.0, 0.45, 1.3)


S['weeping_mother/ambient1'] = mother_ambient(0)
S['weeping_mother/ambient2'] = mother_ambient(1)
d = 0.8
S['weeping_mother/hurt'] = reverb(voice(contour(d, (0, 420), (0.15, 520), (1, 260)), d, WOMAN, breath=0.5, jitter=0.07, rough=0.3) * env(d, 0.01, 0.5)
                                  + place(d, (0.0, crackle(0.3, 90, 1500) * decay(0.3, 0.06) * 0.8), (0.0, creak(0.5, 70, 30) * env(0.5, 0.01, 0.35) * 0.4)),
                                  1.2, 0.35, 0.8)
d = 5.0
fall = contour(3.6, (0, 560), (0.1, 620), (0.5, 380), (1, 120)) * (1 + 0.03 * np.sin(2 * np.pi * 5.5 * t(3.6)))
S['weeping_mother/death'] = reverb(place(d, (0.0, voice(fall, 3.6, WOMAN, breath=0.6, jitter=0.07, rough=0.4) * env(3.6, 0.03, 2.4) * 1.1),
                                         (0.0, voice(fall * 0.5, 3.6, LOG, breath=0.5, jitter=0.08, rough=0.9) * env(3.6, 0.3, 2.0) * 0.7),
                                         (1.4, creak(2.2, 50, 9) * env(2.2, 0.3, 0.3) * 0.8), (3.3, crackle(1.0, 120, 1400) * decay(1.0, 0.3) * 1.0),
                                         (3.5, thump(1.3, 80, 24, 0.3) * 2.4), (3.5, lowpass(noise(0.8), 600) * decay(0.8, 0.15) * 1.2),
                                         (3.6, highpass(noise(1.2), 2000) * env(1.2, 0.05, 1.0) * 0.25)), 2.2, 0.45, 1.5)
d = 2.8
rise = contour(d, (0, 310), (0.14, 560), (0.55, 610), (0.8, 520), (1, 300)) * (1 + 0.035 * np.sin(2 * np.pi * 6.2 * t(d)) * np.clip(t(d) / 0.8, 0, 1))
S['weeping_mother/wail'] = reverb(voice(rise, d, ((950, 160, 1.0), (1550, 220, 0.8), (3100, 380, 0.45)), breath=0.35, jitter=0.05, rough=0.15) * env(d, 0.12, 1.1) * 1.2
                                  + voice(rise * 1.005, d, WOMAN, breath=0.2, jitter=0.06) * env(d, 0.3, 1.2) * 0.6
                                  + voice(rise * 0.5, d, LOG, breath=0.4, jitter=0.08, rough=0.9) * env(d, 0.4, 1.0) * 0.5, 2.6, 0.5, 2.0)
d = 0.7
S['weeping_mother/lash'] = reverb(place(d, (0.0, whoosh(0.5, 500, 4200, 0.75, 0.45) * 1.0), (0.0, whoosh(0.5, 200, 1200, 0.7, 0.5) * 0.7),
                                        (0.37, highpass(noise(0.06), 2200) * decay(0.06, 0.006) * 1.4), (0.37, thump(0.1, 400, 160, 0.02) * 0.6)), 0.6, 0.25, 0.4)
d = 1.3
S['weeping_mother/slam'] = reverb(place(d, (0.0, thump(1.0, 96, 26, 0.2) * 2.4), (0.0, crackle(0.5, 110, 1500) * decay(0.5, 0.1) * 1.0),
                                        (0.0, lowpass(noise(0.4), 900) * decay(0.4, 0.07) * 1.4), (0.03, splash(0.7, 1.2, 3000) * 0.6),
                                        (0.15, creak(0.7, 60, 22) * env(0.7, 0.02, 0.5) * 0.4)), 1.1, 0.35, 0.9)
d = 2.0
S['weeping_mother/creak'] = reverb(creak(d, 14, 44) * env(d, 0.3, 0.5) * 0.9 + creak(d, 9, 20, (240, 610, 1180)) * env(d, 0.5, 0.5) * 0.7
                                   + lowpass(noise(d), 160, 3) * env(d, 0.5, 0.6) * 1.4 + place(d, (1.3, crackle(0.5, 50, 1300) * decay(0.5, 0.15) * 0.5)), 1.4, 0.35, 1.0)
d = 0.9
S['weeping_mother/root'] = reverb(place(d, (0.0, thump(0.7, 120, 34, 0.14) * 2.0), (0.0, lowpass(noise(0.4), 1000) * decay(0.4, 0.08) * 1.4),
                                        (0.0, crackle(0.6, 100, 1300) * decay(0.6, 0.15) * 0.9), (0.05, creak(0.5, 80, 30) * env(0.5, 0.01, 0.35) * 0.5),
                                        (0.1, squelch(0.4, 300) * 0.5)), 0.8, 0.3, 0.6)

S['music/boss'] = boss_music()

# ============================================================== the Black Firwood: men who will not sleep, wax, moths, and a bear kept awake
# (everything of a new place is written after the old: the generator draws its noise from one stream, and what is added
# at the end leaves every earlier sound as it was)
HOARSE = ((480, 120, 1.0), (1250, 180, 0.6), (2600, 320, 0.3))
OLD = ((390, 110, 1.0), (1020, 160, 0.6), (2350, 300, 0.22))
BEAR = ((230, 100, 1.0), (560, 150, 0.9), (1200, 260, 0.4), (2400, 400, 0.14))
SMOTHERED = ((210, 80, 1.0), (430, 100, 0.6), (900, 160, 0.12))


def mutter(d, pitch, formants, syllables, drift=0.04, breath=0.6):
    """A prayer said on one note by a dry mouth: short syllables with the breath showing between them."""
    out = np.zeros(int(SR * d))
    at = 0.05
    for i in range(syllables):
        n = float(rng.uniform(0.11, 0.2))
        f = pitch * (1 + float(rng.uniform(-drift, drift)))
        syl = voice(contour(n, (0, f * 0.97), (0.3, f), (1, f * 0.95)), n, formants, breath=breath, jitter=0.05, rough=0.35) * env(n, 0.02, n * 0.5)
        a = int(at * SR)
        if a + len(syl) >= len(out):
            break
        out[a:a + len(syl)] += syl * float(rng.uniform(0.6, 1.0))
        at += n + float(rng.uniform(0.02, 0.09)) + (0.22 if i % 4 == 3 else 0.0)
    return out


def snore(d, pitch=62, whistle=1500):
    """One breath of a sleeper: the rattle of the breath going in, and the long whistle of it coming out."""
    a = d * 0.45
    rattle_ = lowpass(voice(contour(a, (0, pitch * 0.9), (0.6, pitch * 1.15), (1, pitch)), a, ((160, 70, 1.0), (380, 110, 0.8), (800, 200, 0.3)),
                            breath=0.9, jitter=0.12, rough=1.0), 1100) * env(a, a * 0.35, a * 0.25)
    b = d - a - 0.08
    out_ = moving_bandpass(noise(b), contour(b, (0, whistle * 0.8), (0.3, whistle), (1, whistle * 0.6)), 0.3) * env(b, b * 0.2, b * 0.6) * 0.5
    out_ += tone(contour(b, (0, whistle * 1.5), (1, whistle * 1.1)), b) * env(b, b * 0.3, b * 0.6) * 0.03
    return place(d, (0.0, rattle_), (a + 0.08, out_))


def wingbeats(d, rate, lo=250, hi=2600):
    """Wide soft wings: every beat a puff of air."""
    n = int(SR * d)
    beat = (0.5 + 0.5 * np.sin(2 * np.pi * np.cumsum(np.full(n, rate) * (1 + 0.12 * lowpass(rng.standard_normal(n), 3))) / SR)) ** 3
    return bandpass(noise(d), lo, hi) * beat


def wood_knock(d, f=620, hollow=1.0):
    """A mallet on a hung board: the board's own notes, which are few and clear, and the smack of the wood."""
    ring = (tone(f, d) + 0.55 * tone(f * 2.42, d, 1.0) + 0.3 * tone(f * 4.13, d, 2.0)) * decay(d, 0.07 * hollow)
    smack = bandpass(noise(d), f * 1.5, f * 7) * decay(d, 0.006) * 1.2
    return ring * 0.8 + smack + thump(d, f * 0.35, f * 0.2, 0.04) * 0.7


def chain(d, count, seed=0):
    return rattle(d, count, 1500, 4200, seed=seed)


for k in (0, 1):
    S[f'sect/ambient{k + 1}'] = reverb(mutter(2.2 + 0.4 * k, 112 - 12 * k, HOARSE, 9 + 2 * k) * 0.9, 1.0, 0.3, 0.6)
d = 0.45
S['sect/hurt'] = reverb(voice(contour(d, (0, 150), (0.2, 185), (1, 110)), d, HOARSE, breath=1.0, jitter=0.08, rough=0.6) * env(d, 0.01, 0.3), 0.5, 0.25, 0.3)
d = 2.2
S['sect/death'] = reverb(place(d, (0.0, voice(contour(1.5, (0, 150), (0.2, 132), (1, 70)), 1.5, HOARSE, breath=1.3, jitter=0.06, rough=0.3) * env(1.5, 0.05, 1.1)),
                               (1.0, lowpass(noise(1.1), 900) * env(1.1, 0.4, 0.6) * 0.5), (1.3, thump(0.5, 80, 40, 0.09) * 0.9)), 1.0, 0.3, 0.6)
d = 1.1
S['wakeful/rise'] = reverb(place(d, (0.0, bandpass(noise(0.7), 900, 5000) * env(0.7, 0.2, 0.4) * np.clip(lowpass(noise(0.7), 18) * 5 + 0.6, 0, 1.3) * 0.7),
                                 (0.15, creak(0.4, 60, 24, (500, 1100, 2100)) * env(0.4, 0.02, 0.3) * 0.25),
                                 (0.4, voice(contour(0.55, (0, 96), (1, 142)), 0.55, HOARSE, breath=1.6, jitter=0.05) * env(0.55, 0.3, 0.1) * 0.9)), 0.8, 0.3, 0.5)
d = 1.5
S['wakeful/vigil'] = reverb(voice(contour(d, (0, 104), (0.25, 156), (0.7, 162), (1, 150)), d, ((620, 130, 1.0), (1150, 170, 0.7), (2500, 300, 0.3)),
                                  breath=0.5, jitter=0.04, rough=0.3) * env(d, 0.12, 0.4), 1.3, 0.35, 0.9)
for k in (0, 1):
    S[f'waker/knock{k + 1}'] = reverb(wood_knock(0.5, 560 + 70 * k) * 1.2, 1.2, 0.35, 0.9)
d = 0.7
S['sleeper/cry'] = reverb(place(d, (0.0, voice(contour(0.22, (0, 120), (1, 260)), 0.22, HOARSE, breath=1.8, jitter=0.06) * env(0.22, 0.12, 0.03) * 0.7),
                                (0.2, voice(contour(0.5, (0, 330), (0.15, 380), (1, 200)), 0.5, HOARSE, breath=0.5, jitter=0.09, rough=0.5) * env(0.5, 0.01, 0.35))),
                          0.8, 0.3, 0.5)
d = 0.8
S['sleeper/fall'] = reverb(place(d, (0.0, thump(0.5, 110, 42, 0.09) * 1.8), (0.0, lowpass(noise(0.25), 1200) * decay(0.25, 0.04) * 1.2),
                                 (0.04, wood_knock(0.3, 380, 0.6) * 0.7), (0.12, wood_knock(0.25, 460, 0.5) * 0.35),
                                 (0.1, voice(contour(0.4, (0, 130), (1, 84)), 0.4, HOARSE, breath=1.2, jitter=0.06, rough=0.5) * env(0.4, 0.02, 0.3) * 0.5)), 0.6, 0.25, 0.4)
for k in (0, 1):
    S[f'sleeper/snore{k + 1}'] = reverb(snore(2.3 + 0.3 * k, 66 - 8 * k, 1500 - 250 * k) * 0.9, 0.8, 0.25, 0.5)


# the waxen: a heap of everything their candles shed, with somebody's breath in it
def waxen_ambient(k):
    d = 2.6
    moan = lowpass(voice(contour(d, (0, 74 - 6 * k), (0.4, 88 - 6 * k), (1, 62)), d, SMOTHERED, breath=0.5, jitter=0.07, rough=0.6), 700) * env(d, 0.6, 1.0)
    return reverb(moan * 1.4 + place(d, (0.3, bubbles(1.8, 9 + 3 * k, 140, 420) * 0.6), (0.2, squelch(0.5, 180) * 0.5), (1.5, squelch(0.6, 150) * 0.45),
                                     (0.0, crackle(d, 14, 2600) * 0.12)), 1.0, 0.3, 0.6)


S['waxen/ambient1'] = waxen_ambient(0)
S['waxen/ambient2'] = waxen_ambient(1)
d = 0.6
S['waxen/hurt'] = reverb(place(d, (0.0, squelch(0.35, 300) * 1.2), (0.0, thump(0.3, 140, 60, 0.05) * 1.0),
                               (0.02, lowpass(voice(contour(0.5, (0, 110), (0.2, 128), (1, 76)), 0.5, SMOTHERED, breath=0.6, jitter=0.08, rough=0.7), 800)
                                * env(0.5, 0.01, 0.35) * 1.3)), 0.6, 0.25, 0.4)
d = 2.6
S['waxen/death'] = reverb(place(d, (0.0, lowpass(voice(contour(1.4, (0, 118), (1, 44)), 1.4, SMOTHERED, breath=0.7, jitter=0.09, rough=0.8), 700) * env(1.4, 0.03, 1.0) * 1.3),
                                (0.3, squelch(0.8, 220) * 1.3), (0.35, thump(0.8, 100, 34, 0.14) * 1.5), (0.5, bubbles(1.4, 30, 120, 520) * env(1.4, 0.05, 1.0) * 0.6),
                                (0.6, highpass(noise(1.8), 3800) * decay(1.8, 0.45) * 0.6), (0.6, crackle(1.6, 60, 4200) * decay(1.6, 0.5) * 0.4)), 1.2, 0.3, 0.8)
d = 0.9
S['waxen/slam'] = reverb(place(d, (0.0, thump(0.7, 105, 32, 0.13) * 2.0), (0.0, squelch(0.5, 260) * 1.3), (0.0, lowpass(noise(0.3), 800) * decay(0.3, 0.06) * 1.2),
                               (0.08, bubbles(0.5, 8, 160, 480) * 0.4)), 0.8, 0.3, 0.6)
d = 1.0
S['waxen/gather'] = reverb(moving_bandpass(noise(d), sweep(160, 950, d, 1.6), 0.5) * env(d, 0.7, 0.05) * 1.0 + place(d, (0.1, bubbles(0.85, 26, 140, 700) * env(0.85, 0.5, 0.1) * 0.7))
                           + lowpass(voice(contour(d, (0, 60), (1, 118)), d, SMOTHERED, breath=0.4, jitter=0.07, rough=0.8), 700) * env(d, 0.5, 0.05) * 0.9, 0.6, 0.25, 0.3)
d = 1.5
S['waxen/spew'] = reverb(place(d, (0.0, bandpass(noise(1.3), 500, 4200) * env(1.3, 0.05, 0.6) * np.clip(lowpass(noise(1.3), 40) * 5 + 0.7, 0.1, 1.4) * 1.0),
                               (0.0, bubbles(1.2, 40, 200, 900) * env(1.2, 0.02, 0.6) * 0.6), (0.0, crackle(1.3, 110, 4200) * env(1.3, 0.05, 0.7) * 0.5),
                               (0.0, lowpass(voice(contour(1.1, (0, 128), (1, 86)), 1.1, SMOTHERED, breath=0.5, jitter=0.08, rough=1.0), 800) * env(1.1, 0.04, 0.6) * 1.0),
                               (0.25, squelch(0.5, 320) * 0.6), (0.7, squelch(0.5, 280) * 0.5)), 0.8, 0.3, 0.5)

# the sleep moth: wings as wide as a man's arms, and the dust they shed
for k in (0, 1):
    d = 1.3
    S[f'sleep_moth/flutter{k + 1}'] = reverb(wingbeats(d, 17 + 3 * k) * env(d, 0.15, 0.4) * 1.0, 0.5, 0.2, 0.3)
d = 0.4
S['sleep_moth/hurt'] = reverb(wingbeats(d, 34, 400, 4200) * env(d, 0.01, 0.25) * 1.0 + tone(contour(d, (0, 3300), (0.3, 4100), (1, 2500)), d) * env(d, 0.01, 0.25) * 0.14,
                              0.4, 0.2, 0.3)
d = 1.3
S['sleep_moth/death'] = reverb(place(d, (0.0, bandpass(noise(1.0), 300, 3200) * (0.5 + 0.5 * np.sin(2 * np.pi * np.cumsum(sweep(32, 6, 1.0, 0.6)) / SR)) ** 3
                                         * env(1.0, 0.01, 0.6)), (0.0, tone(contour(0.5, (0, 3600), (1, 1700)), 0.5) * env(0.5, 0.01, 0.4) * 0.12),
                                     (0.75, highpass(noise(0.5), 4500) * env(0.5, 0.05, 0.4) * 0.3), (0.8, thump(0.2, 160, 80, 0.04) * 0.35)), 0.6, 0.25, 0.4)
d = 2.2
S['sleep_moth/dust'] = reverb(place(d, (0.0, highpass(noise(1.8), 5200) * env(1.8, 0.5, 1.1) * 0.5), (0.1, chime(1320, 0.9, 0.6) * 0.5), (0.45, chime(1110, 1.0, 0.6) * 0.45),
                                    (0.85, chime(880, 1.2, 0.5) * 0.4), (0.0, pad_chord((220.0, 277.2), 2.1, 0.7, 1.1, 900) * 0.9)), 1.6, 0.45, 1.2)


# the Shatun: a chest like a barrel, and a chain that has never been off him
def shatun_ambient(k):
    d = 2.4
    huff = voice(contour(1.1, (0, 58 - 4 * k), (0.3, 70), (1, 46)), 1.1, BEAR, breath=1.2, jitter=0.1, rough=1.0) * env(1.1, 0.15, 0.6)
    huff2 = voice(contour(0.7, (0, 62), (1, 44)), 0.7, BEAR, breath=1.5, jitter=0.1, rough=1.0) * env(0.7, 0.1, 0.45)
    return reverb(place(d, (0.1, huff * 1.2), (1.4 + 0.1 * k, huff2 * 0.8), (0.9, chain(0.6, 4, seed=71 + k) * 0.3)), 1.0, 0.3, 0.6)


S['shatun/ambient1'] = shatun_ambient(0)
S['shatun/ambient2'] = shatun_ambient(1)
d = 0.8
S['shatun/hurt'] = reverb(voice(contour(d, (0, 122), (0.15, 150), (1, 64)), d, BEAR, breath=0.6, jitter=0.1, rough=1.0) * env(d, 0.01, 0.5) * 1.2
                          + place(d, (0.1, chain(0.5, 4, seed=73) * 0.25)), 0.9, 0.3, 0.6)
d = 3.4
S['shatun/death'] = reverb(place(d, (0.0, voice(contour(2.4, (0, 138), (0.12, 118), (0.6, 78), (1, 34)), 2.4, BEAR, breath=0.7, jitter=0.11, rough=1.0) * env(2.4, 0.03, 1.7) * 1.3),
                                 (1.7, thump(1.2, 86, 24, 0.24) * 2.2), (1.7, lowpass(noise(0.5), 700) * decay(0.5, 0.1) * 1.4), (1.75, chain(1.0, 9, seed=75) * 0.45),
                                 (2.1, lowpass(noise(1.2), 600) * env(1.2, 0.3, 0.8) * 0.6)), 1.6, 0.35, 1.0)
d = 2.4
S['shatun/roar'] = reverb(voice(contour(d, (0, 56), (0.15, 132), (0.55, 118), (1, 48)), d, ((260, 110, 1.0), (640, 170, 1.0), (1350, 280, 0.6), (2600, 420, 0.22)),
                                breath=0.5, jitter=0.12, rough=1.0) * env(d, 0.12, 1.2) * 1.4
                          + lowpass(noise(d), 240, 3) * env(d, 0.2, 1.2) * 1.4, 1.8, 0.4, 1.2)
d = 0.6
S['shatun/swipe'] = reverb(place(d, (0.0, whoosh(0.5, 160, 1500, 0.35, 0.5) * 1.4), (0.12, highpass(noise(0.2), 2600) * decay(0.2, 0.03) * 0.5),
                                                                          (0.05, voice(contour(0.4, (0, 96), (1, 62)), 0.4, BEAR, breath=0.9, jitter=0.1, rough=1.0) * env(0.4, 0.05, 0.25) * 0.6)),
                           0.5, 0.2, 0.3)
d = 1.4
S['shatun/slam'] = reverb(place(d, (0.0, thump(1.2, 92, 22, 0.26) * 2.6), (0.0, lowpass(noise(0.5), 850) * decay(0.5, 0.1) * 1.8), (0.0, crackle(0.6, 90, 1500) * decay(0.6, 0.15) * 0.8),
                                (0.05, chain(0.7, 8, seed=77) * 0.4), (0.1, lowpass(noise(1.1), 200, 3) * decay(1.1, 0.35) * 2.0)), 1.3, 0.35, 0.9)
d = 0.6
S['shatun/bite'] = reverb(place(d, (0.0, voice(contour(0.3, (0, 90), (1, 150)), 0.3, BEAR, breath=0.8, jitter=0.1, rough=1.0) * env(0.3, 0.15, 0.03) * 0.8),
                                (0.26, highpass(noise(0.07), 1700) * decay(0.07, 0.008) * 1.8), (0.26, thump(0.25, 190, 70, 0.04) * 1.4),
                                (0.27, bandpass(noise(0.1), 700, 2600) * decay(0.1, 0.02) * 1.0)), 0.5, 0.25, 0.3)
d = 0.5
S['shatun/step'] = reverb(place(d, (0.0, thump(0.4, 78, 32, 0.09) * 1.6), (0.0, lowpass(noise(0.2), 500) * decay(0.2, 0.04) * 1.0), (0.06, chain(0.35, 3, seed=79) * 0.22)), 0.5, 0.2, 0.3)
d = 1.9
S['shatun/crash'] = reverb(place(d, (0.0, thump(0.9, 110, 30, 0.18) * 2.4), (0.0, creak(0.5, 110, 30, (300, 720, 1500)) * env(0.5, 0.005, 0.4) * 0.9),
                                 (0.0, crackle(0.9, 130, 1700) * decay(0.9, 0.2) * 1.2), (0.03, lowpass(noise(0.4), 1300) * decay(0.4, 0.07) * 1.5),
                                 (0.1, chain(0.8, 9, seed=81) * 0.4),
                                 (0.5, voice(contour(1.2, (0, 110), (0.2, 96), (1, 58)), 1.2, BEAR, breath=1.0, jitter=0.14, rough=0.8) * env(1.2, 0.1, 0.8) * 0.8)), 1.2, 0.35, 0.8)


# the Elder: the oldest voice of them, and the only one that is heard through the tent
def elder_ambient(k):
    d = 3.2
    return reverb(mutter(d, 84 - 8 * k, OLD, 11 + k, drift=0.025, breath=0.45) * 1.0 + pad_chord((84.0 - 8 * k,), d, 0.8, 1.2, 500) * 0.5, 2.0, 0.45, 1.3)


S['elder/ambient1'] = elder_ambient(0)
S['elder/ambient2'] = elder_ambient(1)
d = 0.6
S['elder/hurt'] = reverb(voice(contour(d, (0, 118), (0.2, 140), (1, 82)), d, OLD, breath=0.9, jitter=0.09, rough=0.7) * env(d, 0.01, 0.4), 1.0, 0.35, 0.7)
d = 3.6
S['elder/death'] = reverb(place(d, (0.0, voice(contour(2.6, (0, 128), (0.15, 112), (1, 52)), 2.6, OLD, breath=1.4, jitter=0.07, rough=0.3) * env(2.6, 0.06, 2.0) * 1.1),
                                (1.2, highpass(noise(1.6), 3600) * decay(1.6, 0.4) * 0.5), (1.9, wood_knock(0.5, 300, 0.8) * 0.8), (2.1, wood_knock(0.4, 360, 0.6) * 0.45),
                                (2.0, thump(0.8, 84, 34, 0.14) * 1.4), (2.3, pad_chord((98.0, 146.8), 1.3, 0.3, 0.9, 700) * 0.7)), 2.2, 0.45, 1.4)
d = 0.8
S['elder/sweep'] = reverb(place(d, (0.0, whoosh(0.7, 140, 1300, 0.4, 0.5) * 1.3), (0.05, bandpass(noise(0.6), 200, 1200) * (0.5 + 0.5 * np.sin(2 * np.pi * 23 * t(0.6))) * env(0.6, 0.15, 0.3) * 0.7),
                                                                        (0.1, crackle(0.5, 70, 3600) * env(0.5, 0.1, 0.3) * 0.3)), 0.7, 0.25, 0.4)
d = 2.6
S['elder/chant'] = reverb(place(d, (0.0, voice(contour(1.2, (0, 98), (0.1, 110), (1, 110)), 1.2, OLD, breath=0.3, jitter=0.03, rough=0.2) * env(1.2, 0.1, 0.2)),
                                (1.15, voice(contour(1.4, (0, 110), (0.12, 147), (0.8, 147), (1, 131)), 1.4, OLD, breath=0.3, jitter=0.03, rough=0.2) * env(1.4, 0.08, 0.6) * 1.1),
                                (0.0, choir((110.0, 165.0), 2.5, 0.6, 0.9) * 0.8)), 2.4, 0.5, 1.5)
d = 1.2
S['elder/knock'] = reverb(place(d, (0.0, wood_knock(0.7, 210, 1.6) * 1.5), (0.0, thump(0.8, 90, 30, 0.16) * 1.8), (0.0, lowpass(noise(0.3), 700) * decay(0.3, 0.06) * 1.0),
                                (0.02, clang(1180, 0.9, strike=0.2) * 0.14)), 1.8, 0.45, 1.2)
d = 1.5
S['elder/call'] = reverb(place(d, (0.0, voice(contour(0.45, (0, 120), (0.3, 168), (1, 150)), 0.45, OLD, breath=0.4, jitter=0.05, rough=0.5) * env(0.45, 0.03, 0.12)),
                               (0.5, voice(contour(0.95, (0, 150), (0.15, 205), (0.6, 190), (1, 120)), 0.95, OLD, breath=0.4, jitter=0.05, rough=0.5) * env(0.95, 0.03, 0.5) * 1.1)),
                         2.2, 0.45, 1.4)
d = 3.4
S['elder/slumber'] = reverb(place(d, (0.0, voice(contour(1.9, (0, 96), (0.3, 170), (0.55, 160), (1, 70)), 1.9, ((640, 140, 1.0), (1100, 180, 0.6), (2400, 300, 0.2)),
                                               breath=1.3, jitter=0.05, rough=0.15) * env(1.9, 0.5, 1.0)),
                                  (1.7, snore(1.6, 54, 1150) * 0.9), (0.6, highpass(noise(1.6), 4000) * decay(1.6, 0.4) * 0.3)), 2.2, 0.45, 1.3)
d = 3.2
S['elder/dream'] = reverb(place(d, (0.0, pad_chord((110.0, 130.8, 164.8), 3.0, 1.2, 1.4, 1100, 0.007, 0.008) * 1.6), (0.3, choir((220.0, 261.6), 2.6, 1.0, 1.3) * 0.5),
                                (0.6, highpass(noise(2.4), 5000) * env(2.4, 1.0, 1.2) * 0.25), (1.1, chime(880, 1.2, 0.5) * 0.25), (1.6, chime(659, 1.4, 0.5) * 0.22),
                                (0.0, lowpass(noise(3.0), 180, 3) * env(3.0, 1.4, 1.2) * 1.2)), 2.6, 0.5, 1.6)
for k in (0, 1):
    S[f'elder/snore{k + 1}'] = reverb(snore(2.9 + 0.3 * k, 52 - 5 * k, 1150 - 150 * k) * 1.0 + pad_chord((52.0,), 3.0 + 0.3 * k, 1.0, 1.4, 300)[:int(SR * (2.9 + 0.3 * k))] * 0.5,
                                      2.0, 0.4, 1.2)


# ============================================================== the Morning Wharf
SALT = ((330, 110, 1.0), (880, 160, 0.7), (1900, 280, 0.3))     # a seaman's throat: wind, salt and shouting over water
BRASS = ((300, 100, 1.0), (760, 150, 0.9), (1500, 260, 0.45), (2700, 400, 0.18))


def sailor_ambient(k):
    # a breath through a throat full of water, and half a word of a hauling song
    d = 2.0 + 0.3 * k
    hum = voice(contour(d, (0, 96 - 8 * k), (0.3, 122 - 8 * k), (0.6, 108 - 8 * k), (1, 88 - 8 * k)), d, SALT, breath=0.9, jitter=0.06, rough=0.5) * env(d, 0.3, 0.8)
    return reverb(hum * 0.9 + place(d, (0.4, bubbles(1.0, 5 + 2 * k, 180, 520) * 0.35)), 0.9, 0.3, 0.5)


S['sailor/ambient1'] = sailor_ambient(0)
S['sailor/ambient2'] = sailor_ambient(1)
d = 0.45
S['sailor/hurt'] = reverb(voice(contour(d, (0, 140), (0.2, 172), (1, 100)), d, SALT, breath=0.9, jitter=0.08, rough=0.7) * env(d, 0.01, 0.3)
                          + place(d, (0.05, bubbles(0.3, 3, 200, 500) * 0.3)), 0.5, 0.25, 0.3)
d = 2.3
S['sailor/death'] = reverb(place(d, (0.0, voice(contour(1.4, (0, 138), (0.2, 120), (1, 62)), 1.4, SALT, breath=1.2, jitter=0.07, rough=0.4) * env(1.4, 0.04, 1.0)),
                                 (0.7, bubbles(1.2, 12, 150, 480) * 0.6), (1.3, thump(0.5, 84, 40, 0.09) * 0.9), (1.3, lowpass(noise(0.4), 900) * decay(0.4, 0.08) * 0.6)),
                           1.0, 0.3, 0.6)
d = 0.7
S['sailor/hook'] = reverb(place(d, (0.0, whoosh(0.35, 400, 2600, 0.5, 0.4) * 1.2), (0.22, clang(1450, 0.35, strike=0.5) * 0.35),
                                (0.22, wood_knock(0.25, 420, 0.5) * 0.6), (0.3, creak(0.3, 90, 40, (600, 1300, 2400)) * env(0.3, 0.01, 0.2) * 0.3)), 0.5, 0.2, 0.3)
d = 0.6
S['sailor/throw'] = reverb(place(d, (0.0, voice(contour(0.2, (0, 150), (1, 110)), 0.2, SALT, breath=1.4, jitter=0.06, rough=0.5) * env(0.2, 0.02, 0.12) * 0.6),
                                 (0.06, whoosh(0.5, 300, 3200, 0.3, 0.35) * 1.3)), 0.5, 0.2, 0.3)
d = 0.8
S['sailor/oar'] = reverb(place(d, (0.0, whoosh(0.7, 110, 900, 0.45, 0.5) * 1.5), (0.1, lowpass(noise(0.5), 500) * env(0.5, 0.15, 0.3) * 0.6)), 0.6, 0.2, 0.3)
d = 0.5
S['harpoon/hit'] = reverb(place(d, (0.0, wood_knock(0.3, 520, 0.5) * 0.9), (0.0, thump(0.3, 150, 60, 0.05) * 1.2), (0.0, clang(1900, 0.3, strike=0.6) * 0.25),
                                (0.04, lowpass(noise(0.2), 1500) * decay(0.2, 0.04) * 0.6)), 0.4, 0.2, 0.3)
d = 1.0
S['pilot/whirl'] = reverb(place(d, *[(0.22 * i, whoosh(0.3, 250, 1800 + 200 * i, 0.5, 0.4) * (0.9 + 0.1 * i)) for i in range(4)],
                                (0.0, bandpass(noise(d), 1800, 5200) * env(d, 0.3, 0.4) * 0.12)), 0.6, 0.2, 0.3)
d = 0.9
S['pilot/lead'] = reverb(place(d, (0.0, thump(0.6, 130, 44, 0.1) * 2.2), (0.0, lowpass(noise(0.3), 900) * decay(0.3, 0.05) * 1.3), (0.0, clang(420, 0.6, strike=0.3) * 0.22),
                               (0.08, chain(0.4, 3, seed=91) * 0.14)), 0.8, 0.3, 0.5)

# what lives in the sand: dry plates, and nothing in it of a voice
d = 0.8
S['sand_crab/dig'] = reverb(place(d, (0.0, bandpass(noise(0.7), 700, 4200) * env(0.7, 0.1, 0.4) * np.clip(lowpass(noise(0.7), 22) * 5 + 0.6, 0, 1.3) * 0.9),
                                  (0.1, thump(0.4, 110, 60, 0.08) * 0.6), (0.2, crackle(0.5, 40, 2400) * 0.5)), 0.4, 0.15, 0.2)
for k in (0, 1):
    S[f'sand_crab/snap{k + 1}'] = reverb(place(0.3, (0.0, wood_knock(0.2, 1500 + 260 * k, 0.25) * 0.9), (0.0, highpass(noise(0.05), 2500) * decay(0.05, 0.006) * 1.1),
                                               (0.012, wood_knock(0.15, 2100 + 200 * k, 0.2) * 0.4)), 0.3, 0.15, 0.2)
    S[f'sand_crab/click{k + 1}'] = reverb(place(0.6, *[(float(0.05 + 0.1 * i + 0.02 * ((i * 7 + k * 3) % 3)), wood_knock(0.08, 2300 + 190 * ((i * 5 + k) % 4), 0.12)
                                                      * (0.3 + 0.12 * (i % 3))) for i in range(5)]), 0.3, 0.15, 0.2)
d = 0.35
S['sand_crab/hurt'] = reverb(place(d, (0.0, wood_knock(0.2, 1100, 0.3) * 0.8), (0.0, crackle(0.3, 90, 3000) * decay(0.3, 0.08) * 0.9),
                                   (0.02, tone(contour(0.25, (0, 2900), (1, 2100)), 0.25) * env(0.25, 0.01, 0.2) * 0.1)), 0.3, 0.15, 0.2)
d = 0.9
S['sand_crab/death'] = reverb(place(d, (0.0, wood_knock(0.3, 760, 0.5) * 1.0), (0.0, crackle(0.5, 80, 2600) * decay(0.5, 0.12) * 1.1), (0.1, squelch(0.4, 320) * 0.6),
                                    (0.4, wood_knock(0.1, 2100, 0.15) * 0.3), (0.52, wood_knock(0.1, 1900, 0.15) * 0.2)), 0.4, 0.2, 0.25)


# the skua: a gull's bigger, darker cousin, which takes what others caught
def bird_cry(d, f0, f1, harsh=0.6, notes=1):
    out = np.zeros(int(SR * d))
    n = d / notes
    for i in range(notes):
        f = contour(n * 0.92, (0, f0 * 0.8), (0.18, f0), (0.7, (f0 + f1) * 0.5), (1, f1))
        note = (tone(f, n * 0.92) + 0.6 * tone(f * 2.0, n * 0.92, 0.4) + 0.35 * tone(f * 3.02, n * 0.92, 1.1)) * env(n * 0.92, 0.02, n * 0.4)
        note = note * (1.0 - harsh + harsh * np.clip(0.55 + 0.45 * np.sin(2 * np.pi * 61 * t(n * 0.92)) + lowpass(noise(n * 0.92), 300) * 2.0, 0, 1.6))
        a = int(i * n * SR)
        out[a:a + len(note)] += note[:len(out) - a] * (1.0 - 0.12 * i)
    return bandpass(out, 500, 6500)


for k in (0, 1):
    S[f'skua/cry{k + 1}'] = reverb(bird_cry(0.9 + 0.2 * k, 1250 - 150 * k, 760 - 80 * k, 0.75, 2 + k) * 0.9, 1.4, 0.3, 0.9)
d = 0.9
S['skua/stoop'] = reverb(place(d, (0.0, bird_cry(0.4, 1500, 1050, 0.8) * 0.8), (0.25, whoosh(0.6, 500, 3600, 0.75, 0.35) * 1.2),
                               (0.2, wingbeats(0.3, 14, 300, 2200) * 0.5)), 0.8, 0.25, 0.5)
d = 0.4
S['skua/hurt'] = reverb(place(d, (0.0, bird_cry(0.3, 1650, 1250, 0.95) * 0.9), (0.0, wingbeats(0.4, 22, 300, 2600) * env(0.4, 0.01, 0.25) * 0.7)), 0.5, 0.2, 0.3)
d = 1.0
S['skua/death'] = reverb(place(d, (0.0, bird_cry(0.55, 1400, 620, 0.9) * 0.9), (0.2, wingbeats(0.6, 18, 250, 2200) * decay(0.6, 0.2) * 0.9),
                                (0.7, thump(0.25, 150, 70, 0.04) * 0.6), (0.7, lowpass(noise(0.2), 1600) * decay(0.2, 0.04) * 0.5)), 0.6, 0.25, 0.4)


# the Helmsman of the Daybreak: a chest like a cask, a chain across it, and an order nobody has obeyed since
def helmsman_ambient(k):
    d = 2.8
    breath = voice(contour(d, (0, 64 - 5 * k), (0.4, 78 - 5 * k), (1, 58 - 5 * k)), d, BRASS, breath=0.9, jitter=0.06, rough=0.8) * env(d, 0.5, 1.0)
    return reverb(breath * 1.2 + place(d, (0.5 + 0.3 * k, chain(0.7, 5, seed=93 + k) * 0.2), (1.6, creak(0.6, 70, 34, (240, 560, 1100)) * env(0.6, 0.05, 0.4) * 0.2)),
                  1.4, 0.35, 0.8)


S['helmsman/ambient1'] = helmsman_ambient(0)
S['helmsman/ambient2'] = helmsman_ambient(1)
d = 0.7
S['helmsman/hurt'] = reverb(voice(contour(d, (0, 104), (0.15, 128), (1, 70)), d, BRASS, breath=0.6, jitter=0.09, rough=1.0) * env(d, 0.01, 0.5) * 1.2
                            + place(d, (0.05, chain(0.4, 4, seed=95) * 0.2)), 1.0, 0.3, 0.6)
d = 3.8
S['helmsman/death'] = reverb(place(d, (0.0, voice(contour(2.4, (0, 118), (0.15, 104), (0.6, 74), (1, 40)), 2.4, BRASS, breath=0.9, jitter=0.1, rough=0.9) * env(2.4, 0.04, 1.7) * 1.3),
                                   (1.5, chain(1.2, 10, seed=97) * 0.35), (2.2, thump(1.2, 80, 26, 0.24) * 2.4), (2.2, clang(210, 1.5, strike=0.3) * 0.5),
                                   (2.25, lowpass(noise(0.6), 800) * decay(0.6, 0.12) * 1.4), (2.5, creak(1.0, 60, 22, (220, 520, 1000)) * env(1.0, 0.05, 0.8) * 0.5)),
                             2.0, 0.4, 1.2)
d = 0.9
S['helmsman/swing'] = reverb(place(d, (0.0, whoosh(0.8, 90, 800, 0.5, 0.55) * 1.8), (0.1, chain(0.6, 6, seed=99) * 0.3),
                                   (0.15, lowpass(noise(0.5), 380) * env(0.5, 0.15, 0.3) * 0.9)), 0.8, 0.25, 0.5)
d = 1.5
S['helmsman/anchor'] = reverb(place(d, (0.0, thump(1.2, 100, 24, 0.3) * 3.0), (0.0, clang(196, 1.4, strike=0.35) * 0.75), (0.0, clang(523, 0.8, strike=0.5) * 0.25),
                                    (0.0, wood_knock(0.5, 240, 1.2) * 1.3), (0.02, lowpass(noise(0.7), 900) * decay(0.7, 0.14) * 1.6),
                                    (0.05, crackle(0.6, 80, 1600) * decay(0.6, 0.16) * 0.8), (0.2, chain(0.8, 7, seed=101) * 0.3)), 1.8, 0.4, 1.1)
for k in (0, 1):
    S[f'helmsman/chain{k + 1}'] = reverb(place(0.7, (0.0, chain(0.6, 6 + 2 * k, seed=103 + k) * 0.6), (0.05, lowpass(chain(0.5, 4, seed=107 + k), 2200) * 0.4),
                                               (0.1, grind(0.4, 300, 1400, seed=11 + k) * env(0.4, 0.05, 0.25) * 0.14)), 0.6, 0.2, 0.4)
d = 2.0
S['helmsman/shout'] = reverb(place(d, (0.0, voice(contour(0.5, (0, 96), (0.3, 150), (1, 140)), 0.5, BRASS, breath=0.4, jitter=0.06, rough=0.9) * env(0.5, 0.03, 0.12)),
                                   (0.55, voice(contour(1.35, (0, 140), (0.1, 196), (0.55, 186), (1, 92)), 1.35, BRASS, breath=0.4, jitter=0.07, rough=1.0) * env(1.35, 0.03, 0.8) * 1.2)),
                             2.4, 0.45, 1.5)


# the Capsized: an old boat's timbers working, and under them something that has no voice at all
def capsized_ambient(k):
    d = 2.6
    return reverb(place(d, (0.0, creak(1.2, 54 - 6 * k, 30, (190, 430, 880)) * env(1.2, 0.1, 0.8) * 0.9), (1.2, creak(0.9, 40, 24, (240, 520, 1040)) * env(0.9, 0.1, 0.6) * 0.6),
                        (0.3, bubbles(1.6, 8 + 2 * k, 120, 380) * 0.5), (0.8 + 0.4 * k, wood_knock(0.12, 1700, 0.14) * 0.25), (1.0 + 0.4 * k, wood_knock(0.12, 1500, 0.14) * 0.2),
                        (0.0, lowpass(noise(d), 240, 3) * env(d, 0.8, 1.0) * 0.6)), 1.2, 0.35, 0.8)


S['capsized/ambient1'] = capsized_ambient(0)
S['capsized/ambient2'] = capsized_ambient(1)
d = 0.7
S['capsized/hurt'] = reverb(place(d, (0.0, wood_knock(0.4, 300, 1.0) * 1.2), (0.0, crackle(0.5, 80, 1800) * decay(0.5, 0.12) * 1.0),
                                  (0.05, creak(0.5, 120, 50, (300, 700, 1500)) * env(0.5, 0.01, 0.4) * 0.6), (0.0, squelch(0.4, 220) * 0.7)), 0.8, 0.3, 0.5)
d = 3.6
S['capsized/death'] = reverb(place(d, (0.0, creak(1.8, 110, 26, (200, 480, 980)) * env(1.8, 0.02, 1.4) * 1.2), (0.6, crackle(1.4, 70, 1500) * env(1.4, 0.2, 0.9) * 1.2),
                                   (1.5, thump(1.3, 74, 24, 0.3) * 2.6), (1.5, wood_knock(0.7, 170, 1.8) * 1.4), (1.55, splash(1.2, 1.6, 3600) * 1.1),
                                   (1.9, bubbles(1.5, 16, 110, 420) * 0.7), (2.4, wood_knock(0.3, 420, 0.6) * 0.4)), 1.8, 0.4, 1.1)
d = 2.0
S['capsized/rise'] = reverb(place(d, (0.0, bandpass(noise(1.4), 500, 3800) * env(1.4, 0.3, 0.8) * np.clip(lowpass(noise(1.4), 16) * 5 + 0.7, 0, 1.4) * 1.0),
                                  (0.2, creak(1.4, 44, 96, (210, 470, 940)) * env(1.4, 0.2, 0.5) * 1.1), (0.5, splash(1.0, 1.4, 3600) * 0.8),
                                  (0.9, thump(0.9, 70, 30, 0.2) * 1.6), (1.3, bubbles(0.6, 8, 150, 500) * 0.5)), 1.4, 0.35, 0.9)
d = 0.6
S['capsized/snap'] = reverb(place(d, (0.0, whoosh(0.25, 300, 2400, 0.7, 0.4) * 1.0), (0.16, wood_knock(0.35, 640, 0.5) * 1.5), (0.16, highpass(noise(0.06), 1800) * decay(0.06, 0.008) * 1.6),
                                  (0.16, thump(0.3, 180, 70, 0.05) * 1.3), (0.19, wood_knock(0.2, 980, 0.3) * 0.5)), 0.7, 0.25, 0.4)
for k in (0, 1):
    S[f'capsized/creak{k + 1}'] = reverb(place(1.1, (0.0, creak(1.0, 70 + 26 * k, 34 + 10 * k, (230 + 40 * k, 540, 1120)) * env(1.0, 0.06, 0.6) * 1.2),
                                               (0.2, lowpass(noise(0.7), 420) * env(0.7, 0.2, 0.4) * 0.5), (0.5, crackle(0.5, 30, 1600) * 0.35)), 1.0, 0.3, 0.6)
d = 1.6
S['capsized/slam'] = reverb(place(d, (0.0, thump(1.3, 86, 22, 0.32) * 3.0), (0.0, wood_knock(0.8, 150, 2.0) * 1.7), (0.0, lowpass(noise(0.6), 1000) * decay(0.6, 0.12) * 1.8),
                                  (0.02, splash(1.2, 1.6, 4200) * 1.1), (0.1, crackle(0.7, 70, 1500) * decay(0.7, 0.2) * 0.9)), 1.8, 0.4, 1.1)
for k in (0, 1):
    S[f'capsized/knock{k + 1}'] = reverb(place(0.6, (0.0, wood_knock(0.5, 250 + 40 * k, 1.5) * 1.5), (0.0, thump(0.4, 110, 50, 0.08) * 1.0),
                                               (0.0, highpass(noise(0.04), 1500) * decay(0.04, 0.006) * 0.8)), 1.2, 0.35, 0.8)


# the shore itself: surf on sand that never quite draws back, gulls, and what the sea does to whoever swims
def surf(d, waves):
    """A loop: `waves` swells in it, each a hiss that climbs the sand and a slower suck back; it ends as it begins."""
    n = int(SR * d)
    x = np.arange(n) / n
    swell = np.zeros(n)
    for i in range(waves):
        c = (i + 0.37 + 0.11 * np.sin(i * 2.3)) / waves
        w = 0.085 + 0.02 * np.cos(i * 1.7)
        dist = np.minimum(np.abs(x - c), 1.0 - np.abs(x - c))
        rise = np.where(((x - c + 0.5) % 1.0) < 0.5, np.exp(-(dist / (w * 0.55)) ** 2), np.exp(-(dist / (w * 1.7)) ** 2))
        swell += rise * (0.8 + 0.2 * np.cos(i * 3.1))
    base = noise(d)
    hiss = bandpass(base, 900, 6000) * swell ** 1.5
    wash = lowpass(noise(d), 500, 3) * (0.35 + 0.65 * swell) * 2.2
    far = lowpass(noise(d), 140, 3) * 2.6
    out = hiss * 0.9 + wash + far
    # (the ends are the same noise seen through the same swell: a short crossfade makes the join seamless)
    k = int(SR * 0.25)
    out[:k] = out[:k] * np.linspace(0, 1, k) + out[-k:] * np.linspace(1, 0, k)
    return out[:n - k]


S['shore/surf'] = surf(9.25, 2)
for k in (0, 1, 2):
    S[f'shore/gull{k + 1}'] = reverb(place(2.4, *[(0.1 + 0.34 * i + 0.05 * ((i + k) % 2), bird_cry(0.3, 1900 - 160 * k + 60 * ((i * 3 + k) % 3), 1500 - 140 * k, 0.35) * (0.22 - 0.03 * i))
                                                  for i in range(3 + k)]), 2.6, 0.6, 1.6, 3800) * 0.6
d = 1.6
S['player/undertow'] = reverb(place(d, (0.0, lowpass(noise(1.5), 320, 3) * env(1.5, 0.5, 0.7) * 2.4), (0.2, bubbles(1.2, 18, 140, 560) * 0.8),
                                    (0.0, thump(1.4, 62, 30, 0.5) * 1.3), (0.4, whoosh(1.0, 120, 700, 0.6, 0.6) * 1.0)), 1.0, 0.3, 0.6)


# ============================================================== events -> files
EVENTS = {
    'entity.withered.ambient': ['withered/ambient1', 'withered/ambient2', 'withered/ambient3'],
    'entity.withered.hurt': ['withered/hurt1', 'withered/hurt2'],
    'entity.withered.death': ['withered/death'],
    'entity.rust_knight.ambient': ['rust_knight/ambient1', 'rust_knight/ambient2'],
    'entity.rust_knight.hurt': ['rust_knight/hurt'],
    'entity.rust_knight.death': ['rust_knight/death'],
    'entity.rust_knight.step': ['rust_knight/step1', 'rust_knight/step2'],
    'entity.overseer.ambient': ['overseer/ambient1', 'overseer/ambient2'],
    'entity.overseer.hurt': ['overseer/hurt'],
    'entity.overseer.death': ['overseer/death'],
    'entity.overseer.roar': ['overseer/roar'],
    'entity.overseer.slam': ['overseer/slam'],
    'entity.overseer.swing': ['overseer/swing'],
    'entity.overseer.chain': ['overseer/chain'],
    'entity.chaplain.ambient': ['chaplain/ambient1', 'chaplain/ambient2'],
    'entity.chaplain.hurt': ['chaplain/hurt'],
    'entity.chaplain.death': ['chaplain/death'],
    'entity.chaplain.cast': ['chaplain/cast'],
    'entity.chaplain.bell': ['chaplain/bell'],
    'entity.chaplain.blink': ['chaplain/blink'],
    'entity.hound.ambient': ['hound/ambient1', 'hound/ambient2'],
    'entity.hound.hurt': ['hound/hurt'],
    'entity.hound.death': ['hound/death'],
    'entity.hound.bite': ['hound/bite'],
    'entity.watchman.alarm': ['watchman/alarm'],
    'entity.gravedigger.ambient': ['gravedigger/ambient1', 'gravedigger/ambient2'],
    'entity.gravedigger.hurt': ['gravedigger/hurt'],
    'entity.gravedigger.death': ['gravedigger/death'],
    'entity.gravedigger.dig': ['gravedigger/dig'],
    'entity.gravedigger.raise': ['gravedigger/raise'],
    'entity.quarry.pick': ['quarry/pick1', 'quarry/pick2'], 'entity.quarry.blast': ['quarry/blast'], 'entity.quarry.fuse': ['quarry/fuse'],
    'entity.quarry.rockfall': ['quarry/rockfall1', 'quarry/rockfall2'], 'entity.quarry.barrow': ['quarry/barrow'],
    'entity.crawler.ambient': ['crawler/ambient1', 'crawler/ambient2'], 'entity.crawler.hurt': ['crawler/hurt'],
    'entity.crawler.death': ['crawler/death'], 'entity.crawler.screech': ['crawler/screech'],
    'entity.unhewn.ambient': ['unhewn/ambient1', 'unhewn/ambient2'], 'entity.unhewn.hurt': ['unhewn/hurt'], 'entity.unhewn.death': ['unhewn/death'],
    'entity.unhewn.wake': ['unhewn/wake'], 'entity.unhewn.slam': ['unhewn/slam'], 'entity.unhewn.step': ['unhewn/step'],
    'entity.taskmaster.ambient': ['taskmaster/ambient1', 'taskmaster/ambient2'], 'entity.taskmaster.hurt': ['taskmaster/hurt'],
    'entity.taskmaster.death': ['taskmaster/death'], 'entity.taskmaster.shout': ['taskmaster/shout'], 'entity.taskmaster.maul': ['taskmaster/maul'],
    'entity.taskmaster.wedge': ['taskmaster/wedge'], 'entity.taskmaster.crack': ['taskmaster/crack'],
    'entity.stone_eater.ambient': ['stone_eater/ambient1', 'stone_eater/ambient2'], 'entity.stone_eater.hurt': ['stone_eater/hurt'],
    'entity.stone_eater.death': ['stone_eater/death'], 'entity.stone_eater.roar': ['stone_eater/roar'], 'entity.stone_eater.burrow': ['stone_eater/burrow'],
    'entity.stone_eater.erupt': ['stone_eater/erupt'], 'entity.stone_eater.bite': ['stone_eater/bite'],
    'entity.pike.ambient': ['pike/ambient1', 'pike/ambient2'], 'entity.pike.hurt': ['pike/hurt'], 'entity.pike.death': ['pike/death'],
    'entity.pike.bite': ['pike/bite'], 'entity.pike.leap': ['pike/leap'], 'entity.pike.flop': ['pike/flop'],
    'entity.lanky.ambient': ['lanky/ambient1', 'lanky/ambient2'], 'entity.lanky.hurt': ['lanky/hurt'], 'entity.lanky.death': ['lanky/death'],
    'entity.lanky.slam': ['lanky/slam'], 'entity.lanky.step': ['lanky/step'],
    'entity.wisp.ambient': ['wisp/ambient1', 'wisp/ambient2'], 'entity.wisp.hurt': ['wisp/hurt'], 'entity.wisp.death': ['wisp/death'],
    'entity.wisp.cast': ['wisp/cast'],
    'entity.wallower.ambient': ['wallower/ambient1', 'wallower/ambient2'], 'entity.wallower.hurt': ['wallower/hurt'],
    'entity.wallower.death': ['wallower/death'], 'entity.wallower.roar': ['wallower/roar'], 'entity.wallower.bite': ['wallower/bite'],
    'entity.wallower.slap': ['wallower/slap'], 'entity.wallower.spit': ['wallower/spit'], 'entity.wallower.plop': ['wallower/plop'],
    'entity.wallower.dive': ['wallower/dive'], 'entity.wallower.wake': ['wallower/wake'], 'entity.wallower.breach': ['wallower/breach'],
    'entity.wallower.gulp': ['wallower/gulp'],
    'entity.weeping_mother.ambient': ['weeping_mother/ambient1', 'weeping_mother/ambient2'], 'entity.weeping_mother.hurt': ['weeping_mother/hurt'],
    'entity.weeping_mother.death': ['weeping_mother/death'], 'entity.weeping_mother.wail': ['weeping_mother/wail'],
    'entity.weeping_mother.lash': ['weeping_mother/lash'], 'entity.weeping_mother.slam': ['weeping_mother/slam'],
    'entity.weeping_mother.creak': ['weeping_mother/creak'], 'entity.weeping_mother.root': ['weeping_mother/root'],
    'entity.sect.ambient': ['sect/ambient1', 'sect/ambient2'], 'entity.sect.hurt': ['sect/hurt'], 'entity.sect.death': ['sect/death'],
    'entity.wakeful.rise': ['wakeful/rise'], 'entity.wakeful.vigil': ['wakeful/vigil'], 'entity.waker.knock': ['waker/knock1', 'waker/knock2'],
    'entity.sleeper.cry': ['sleeper/cry'], 'entity.sleeper.fall': ['sleeper/fall'], 'entity.sleeper.snore': ['sleeper/snore1', 'sleeper/snore2'],
    'entity.waxen.ambient': ['waxen/ambient1', 'waxen/ambient2'], 'entity.waxen.hurt': ['waxen/hurt'], 'entity.waxen.death': ['waxen/death'],
    'entity.waxen.slam': ['waxen/slam'], 'entity.waxen.gather': ['waxen/gather'], 'entity.waxen.spew': ['waxen/spew'],
    'entity.sleep_moth.flutter': ['sleep_moth/flutter1', 'sleep_moth/flutter2'], 'entity.sleep_moth.hurt': ['sleep_moth/hurt'],
    'entity.sleep_moth.death': ['sleep_moth/death'], 'entity.sleep_moth.dust': ['sleep_moth/dust'],
    'entity.shatun.ambient': ['shatun/ambient1', 'shatun/ambient2'], 'entity.shatun.hurt': ['shatun/hurt'], 'entity.shatun.death': ['shatun/death'],
    'entity.shatun.roar': ['shatun/roar'], 'entity.shatun.swipe': ['shatun/swipe'], 'entity.shatun.slam': ['shatun/slam'],
    'entity.shatun.bite': ['shatun/bite'], 'entity.shatun.step': ['shatun/step'], 'entity.shatun.crash': ['shatun/crash'],
    'entity.elder.ambient': ['elder/ambient1', 'elder/ambient2'], 'entity.elder.hurt': ['elder/hurt'], 'entity.elder.death': ['elder/death'],
    'entity.elder.sweep': ['elder/sweep'], 'entity.elder.chant': ['elder/chant'], 'entity.elder.knock': ['elder/knock'],
    'entity.elder.call': ['elder/call'], 'entity.elder.slumber': ['elder/slumber'], 'entity.elder.dream': ['elder/dream'],
    'entity.elder.snore': ['elder/snore1', 'elder/snore2'],
    'entity.sailor.ambient': ['sailor/ambient1', 'sailor/ambient2'], 'entity.sailor.hurt': ['sailor/hurt'], 'entity.sailor.death': ['sailor/death'],
    'entity.sailor.hook': ['sailor/hook'], 'entity.sailor.throw': ['sailor/throw'], 'entity.sailor.oar': ['sailor/oar'],
    'entity.harpoon.hit': ['harpoon/hit'], 'entity.pilot.whirl': ['pilot/whirl'], 'entity.pilot.lead': ['pilot/lead'],
    'entity.sand_crab.dig': ['sand_crab/dig'], 'entity.sand_crab.snap': ['sand_crab/snap1', 'sand_crab/snap2'],
    'entity.sand_crab.click': ['sand_crab/click1', 'sand_crab/click2'], 'entity.sand_crab.hurt': ['sand_crab/hurt'],
    'entity.sand_crab.death': ['sand_crab/death'],
    'entity.skua.cry': ['skua/cry1', 'skua/cry2'], 'entity.skua.stoop': ['skua/stoop'], 'entity.skua.hurt': ['skua/hurt'], 'entity.skua.death': ['skua/death'],
    'entity.helmsman.ambient': ['helmsman/ambient1', 'helmsman/ambient2'], 'entity.helmsman.hurt': ['helmsman/hurt'],
    'entity.helmsman.death': ['helmsman/death'], 'entity.helmsman.swing': ['helmsman/swing'], 'entity.helmsman.anchor': ['helmsman/anchor'],
    'entity.helmsman.chain': ['helmsman/chain1', 'helmsman/chain2'], 'entity.helmsman.shout': ['helmsman/shout'],
    'entity.capsized.ambient': ['capsized/ambient1', 'capsized/ambient2'], 'entity.capsized.hurt': ['capsized/hurt'],
    'entity.capsized.death': ['capsized/death'], 'entity.capsized.rise': ['capsized/rise'], 'entity.capsized.snap': ['capsized/snap'],
    'entity.capsized.creak': ['capsized/creak1', 'capsized/creak2'], 'entity.capsized.slam': ['capsized/slam'],
    'entity.capsized.knock': ['capsized/knock1', 'capsized/knock2'],
    'ambient.shore.surf': ['shore/surf'], 'ambient.shore.gull': ['shore/gull1', 'shore/gull2', 'shore/gull3'],
    'player.undertow': ['player/undertow'],
    'entity.keykeeper.ambient': ['keykeeper/ambient1', 'keykeeper/ambient2'],
    'entity.keykeeper.give': ['keykeeper/give'],
    'ui.death': ['ui/death'], 'ui.boss_defeated': ['ui/boss_defeated'], 'ui.hearth_lit': ['ui/hearth_lit'], 'ui.level_up': ['ui/level_up'],
    'ui.souls_gain': ['ui/souls_gain'], 'ui.souls_retrieve': ['ui/souls_retrieve'], 'ui.place': ['ui/place'],
    'block.hearth.rest': ['block/hearth_rest'], 'block.fog_gate.pass': ['block/fog_gate_pass'],
    'item.flask.drink': ['item/flask_drink'], 'item.flask.empty': ['item/flask_empty'],
    'item.spell.shard': ['item/spell_shard'], 'item.spell.shard_hit': ['item/spell_shard_hit'], 'item.spell.mend': ['item/spell_mend'],
    'item.spell.rebuke': ['item/spell_rebuke'], 'item.spell.fire': ['item/spell_fire'], 'item.spell.fire_burst': ['item/spell_fire_burst'],
    'item.spell.fizzle': ['item/spell_fizzle'],
    'player.roll': ['player/roll'], 'player.backstab': ['player/backstab'], 'player.guard_break': ['player/guard_break'],
    'player.weapon_swing_heavy': ['player/weapon_swing_heavy'],
    'music.boss': ['music/boss'],
}

if __name__ == '__main__':
    check = sys.argv[1] if len(sys.argv) > 1 else None
    used = {n for names in EVENTS.values() for n in names}
    assert used == set(S), (used - set(S), set(S) - used)
    report = []
    # SOUNDS_NEW=1: only what has no file yet is written (an encoder never writes the same file twice, and nothing is
    # gained by every sound of the mod changing in the repository whenever one is added)
    only_new = bool(os.environ.get('SOUNDS_NEW'))
    for name, x in S.items():
        if only_new and os.path.exists(f'{A}/sounds/{name}.ogg'):
            continue
        x = highpass(np.asarray(x, float), 25)
        if name not in ('music/boss', 'shore/surf'):                # (a loop keeps its ends)
            x[:64] *= np.linspace(0, 1, 64)
            x[-400:] *= np.linspace(1, 0, 400)
        x = x / (np.max(np.abs(x)) + 1e-9) * (0.8 if name.startswith('music') else 0.89)
        pcm = (x * 32767).astype(np.int16)
        out = f'{A}/sounds/{name}.ogg'
        os.makedirs(os.path.dirname(out), exist_ok=True)
        wav = (check + '/' + name.replace('/', '_') + '.wav') if check else tempfile.mktemp(suffix='.wav')
        wavfile.write(wav, SR, pcm)
        subprocess.run(['ffmpeg', '-y', '-loglevel', 'error', '-i', wav, '-ac', '1', '-c:a', 'libvorbis', '-q:a', '4' if name.startswith('music') else '5', out],
                       check=True)
        f, pxx = signal.welch(x, SR, nperseg=4096)
        report.append((name, len(x) / SR, float(np.sqrt(np.mean(x ** 2))), float(np.sum(f * pxx) / np.sum(pxx)), os.path.getsize(out)))
        if not check:
            os.remove(wav)
    sounds = {}
    for event, names in EVENTS.items():
        entries = [{'name': f'{MOD}:{n}', 'stream': True} if n.startswith('music') or n == 'shore/surf' else f'{MOD}:{n}' for n in names]
        sounds[event] = {'subtitle': f'subtitles.{MOD}.{event}', 'sounds': entries}
    with open(f'{A}/sounds.json', 'w') as fh:
        json.dump(sounds, fh, indent=2)
        fh.write('\n')
    for r in report:
        print('%-30s %5.2fs rms=%.3f centroid=%5.0fHz %7d bytes' % r)
    if check:
        import matplotlib
        matplotlib.use('Agg')
        import matplotlib.pyplot as plt
        names = [n for n in S if n != 'music/boss']
        rows = -(-len(names) // 9)
        fig, axes = plt.subplots(rows, 9, figsize=(36, 3 * rows))
        axes = axes.ravel()
        for ax, name in zip(axes, names):
            x = np.asarray(S[name], float)
            ax.specgram(x / (np.max(np.abs(x)) + 1e-9), NFFT=1024, Fs=SR, noverlap=768, cmap='magma', vmin=-110, vmax=-30)
            ax.set_ylim(0, 6000)
            ax.set_title(name, fontsize=10)
        for ax in axes[len(names):]:
            ax.axis('off')
        plt.tight_layout()
        plt.savefig(check + '/spectrograms.png', dpi=60)
        fig, ax = plt.subplots(1, 1, figsize=(20, 5))
        x = np.asarray(S['music/boss'], float)
        ax.specgram(x / (np.max(np.abs(x)) + 1e-9), NFFT=4096, Fs=SR, noverlap=2048, cmap='magma', vmin=-110, vmax=-30)
        ax.set_ylim(0, 3000)
        plt.tight_layout()
        plt.savefig(check + '/music.png', dpi=60)
