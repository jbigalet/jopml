import sys

binops = ['add', 'or', 'and', 'xor', 'sub', 'mul']
table = {
    'zero': '0',
    'pc': '1',
    'at': '32',
    'v0': '2', 'v1': '3',
    'a0': '4', 'a1': '5', 'a2': '6', 'a3': '7',
    't0': '8', 't1': '9', 't2': '10', 't3': '11', 't4': '12', 't5': '13', 't6': '14', 't7': '15', 't8': '24', 't9': '25',
    's0': '16', 's1': '17', 's2': '18', 's3': '19', 's4': '20', 's5': '21', 's6': '22', 's7': '23',
    'k0': '26', 'k1': '27',
    'gp': '28',
    'sp': '29',
    'fp': '30',
    'ra': '31'
}
def reg(x):
    if x[0] != "$":
        raise ValueError("%s is not a valid register." % x)
    else:
        return "$%d" % int(table.get(x[1:], x[1:]))
def addr(x):
    l = x.split("(", 1)
    try:
        ofs = int(l[0])
    except ValueError:
        ofs = "#" + l[0] + "#"
    r = reg(l[1][:-1])
    return (r, ofs)
def cvt(op, a):
    if op in binops:
        return "$0 %s %s %s %s" % (op, reg(a[0]), reg(a[1]), reg(a[2]))
    if (op[:-1] in binops) and op[-1] == "i":
        return "$0 %s %s %s %s" % (op[:-1], reg(a[0]), reg(a[1]), int(a[2]))
    if op in ["lw", "sw"]:
        r,ofs = addr(a[1])
        if ofs != 0:
            if ofs % 4 != 0: raise ValueError("Offset %d is not multiple of 4" % ofs)
            return cvt("addi", ['$at', r, ofs/4]) + ("\n$0 %s %s %s" % (op, reg(a[0]), reg('$at')))
        else:
            return "$0 %s %s %s" % (op, reg(a[0]), r)
    if op == "li":
        return "$0 or %s $0 %s" % (reg(a[0]), a[1])
    if op == "j":
        return cvt("li", ["$pc", "#%s#" % a[0]])
    if op == "move":
        return "$0 or %s $0 %s" % (reg(a[0]), reg(a[1]))
    if op == "jr":
        return cvt("move", ["$pc", a[0]])
    if op == "jal":
        return cvt("li", ["$ra", "#HERE#"]) + "\n" + cvt("j", [a[0]])
    if op == "beqz":
        return "%s or %s $0 %s" % (reg(a[0]), reg('$pc'), "#%s#" % a[1])
    return ""
        

def convert_all(s):
    r = ""
    for i in s.split("\n"):
        l = i.split(" ", 1)
        t = l[0].strip()
        if t == "": continue
        if len(l) == 1 and t[-1] == ":":
            r += "#%s#\n" % t[:-1]
        else:
            args = [x.strip() for x in l[1].split(",")]
            r += cvt(t, args) + "\n"
    ln = 0
    labels = {}
#    print(r)
    nr = ""
    for i in r.split("\n"):
        if len(i) > 0 and i[0] == "#" and i[-1] == "#":
            labels[i] = ln
        elif i != "":
            nr += i.replace("#HERE#", str(ln + 2)) + "\n"
            ln += 1
 #   print(nr)
 #   print(labels)
    for (k,v) in labels.items():
        nr = nr.replace(k, str(v))
    return nr

print(convert_all(open(sys.argv[1], "r").read()))
