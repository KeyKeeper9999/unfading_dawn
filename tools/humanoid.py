"""The vanilla humanoid mesh (the one HumanoidModel.createMesh builds) described for modelkit, so that mobs built on it
can be laid out, painted and previewed. The base parts are marked emit=False unless they are replaced."""
from modelkit import Box, Model


def humanoid(tex_h=64, slim_arms=False):
    """64 x tex_h texture in the layout of a player skin: every limb has its own piece of the texture."""
    m = Model(64, tex_h)
    m.part('head', None, (0, 0, 0), boxes=[Box(-4, -8, -4, 8, 8, 8, 'head', uv=(0, 0))], emit=False)
    m.part('hat', 'head', (0, 0, 0), boxes=[Box(-4, -8, -4, 8, 8, 8, 'hat', inflate=0.5, uv=(32, 0))], emit=False)
    m.part('body', None, (0, 0, 0), boxes=[Box(-4, 0, -2, 8, 12, 4, 'body', uv=(16, 16))], emit=False)
    if slim_arms:
        m.part('right_arm', None, (-5, 2, 0), boxes=[Box(-2, -2, -2, 3, 12, 4, 'arm', uv=(40, 16))])
        m.part('left_arm', None, (5, 2, 0), boxes=[Box(-1, -2, -2, 3, 12, 4, 'arm', uv=(32, 48))])
    else:
        m.part('right_arm', None, (-5, 2, 0), boxes=[Box(-3, -2, -2, 4, 12, 4, 'arm', uv=(40, 16))])
        m.part('left_arm', None, (5, 2, 0), boxes=[Box(-1, -2, -2, 4, 12, 4, 'arm', uv=(32, 48))])
    m.part('right_leg', None, (-1.9, 12, 0), boxes=[Box(-2, 0, -2, 4, 12, 4, 'leg', uv=(0, 16))])
    m.part('left_leg', None, (1.9, 12, 0), boxes=[Box(-2, 0, -2, 4, 12, 4, 'leg', uv=(16, 48))])
    return m


def box_of(model, part, index=0):
    return model.parts[part]['boxes'][index]
