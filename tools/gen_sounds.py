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
def knight_ambient(k):
    d = 2.2
    breath_in = moving_bandpass(noise(0.9), sweep(350, 800, 0.9), 0.45) * env(0.9, 0.35, 0.3)
    breath_out = moving_bandpass(noise(1.1), sweep(700, 300, 1.1), 0.45) * env(1.1, 0.2, 0.7)
    x = place(d, (0.0, breath_in * 0.8), (1.0 + 0.05 * k, breath_out))
    # the helmet rings faintly with every breath
    ring = signal.fftconvolve(x, clang(410 + 40 * k, 0.4, strike=0.0) * 0.02)[:len(x)]
    return reverb(x + ring, 0.5, 0.25, 0.3)


S['rust_knight/ambient1'] = knight_ambient(0)
S['rust_knight/ambient2'] = knight_ambient(1)
d = 0.7
S['rust_knight/hurt'] = reverb(clang(330, d) * 0.9 + place(d, (0.03, clang(520, 0.5) * 0.5))
                               + voice(sweep(120, 90, d), d, ((400, 120, 1.0), (900, 150, 0.4)), breath=0.5, rough=0.5) * env(d, 0.01, 0.5) * 0.5,
                               0.5, 0.22, 0.3)
d = 2.4
S['rust_knight/death'] = reverb(place(d, (0.0, voice(sweep(130, 60, 1.2, 0.7), 1.2, ((400, 120, 1.0), (900, 150, 0.4)), breath=0.6, rough=0.6)
                                          * env(1.2, 0.02, 0.9) * 0.7),
                                      (0.5, clang(260, 0.8)), (0.75, clang(390, 0.6) * 0.7), (0.95, clang(180, 0.9) * 0.9), (1.2, clang(470, 0.5) * 0.5),
                                      (0.95, thump(0.5, 80, 35, 0.14) * 1.4), (1.45, clang(300, 0.7) * 0.4)), 0.9, 0.3, 0.5)
for k in range(2):
    d = 0.3
    S[f'rust_knight/step{k + 1}'] = place(d, (0.0, clang(700 + 150 * k, 0.16) * 0.5), (0.04, clang(1100 - 100 * k, 0.12) * 0.35),
                                          (0.0, thump(0.18, 70, 45, 0.05) * 0.8), (0.07, clang(520, 0.14) * 0.25))
d = 0.55
S['player/weapon_swing_heavy'] = whoosh(d, 250, 1300, 0.5, 0.5) + whoosh(d, 80, 300, 0.5, 0.6) * 0.8

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


S['music/boss'] = boss_music()

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
    for name, x in S.items():
        x = highpass(np.asarray(x, float), 25)
        if name != 'music/boss':
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
        entries = [{'name': f'{MOD}:{n}', 'stream': True} if n.startswith('music') else f'{MOD}:{n}' for n in names]
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
        fig, axes = plt.subplots(8, 7, figsize=(28, 24))
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
