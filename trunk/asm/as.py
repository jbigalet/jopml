import sys
word_size = int(sys.argv[1])
binops = {
    'or': 0,
    'and': 1,
    'xor': 2,
    'not': 3,
    'add': 2,
    'neg': 5,
    'sub': 6,
    'mul': 7
}
def parse_reg(s):
    if s[0] == "$":
        return int(s[1:])
    raise ValueError("Error: %s is not a register" % s)
def parse_imm(s):
    if s[0] == "$":
        return (0, int(s[1:]))
    else:
        return (1, int(s))
def pad_word(x):
    return x + ([0]*(word_size - len(x)))
def bin(x):
    if x == 0: return []
    elif x % 2 == 0: return [0] + bin(x/2)
    else: return [1] + bin((x-1)/2)
def instr(a0, t, a1, a2, a3):
    return list(map(pad_word, [bin(a0), t, bin(a1), bin(a2), bin(a3)]))

def convert_instr(i):
    a0 = parse_reg(i[0])
    if i[1] in binops:
        a1 = parse_reg(i[2])
        a2 = parse_reg(i[3])
        imm,a3 = parse_imm(i[4])
        return instr(a0, [imm, 0, 0] + bin(binops[i[1]]), a1, a2, a3)
    elif i[1] == "lw":
        a1 = parse_reg(i[2])
        imm,a3 = parse_imm(i[3])
        return instr(a0, [imm, 1, 0], a1, 0, a3)
    elif i[1] == "sw":
        a2 = parse_reg(i[2])
        imm,a3 = parse_imm(i[3])
        return instr(a0, [imm, 0, 1], 0, a2, a3)
    else:
        raise ValueError("Unknown instruction %s" % i)

def convert(s):
    lines = [x.split(" ") for x in s.split("\n") if x != ""]
    return list(map(convert_instr, lines))

def to_string(b):
    s = ""
    for i in b:
        for j in i:
            for k in j:
                s += str(k)
    return s
print(to_string(convert(open(sys.argv[2], "r").read())))
