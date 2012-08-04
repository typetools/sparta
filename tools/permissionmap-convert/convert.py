#usage: python convert.py
import re
f = open("APICalls.txt", "r")
lines = f.readlines()[1:]
f.close()
prev_package = ""
prev_cls = ""
for line in lines:
    sp = line.strip().split("\t")
    api = sp[0]
    sp_m = api.split("(")
    if(len(sp_m) < 2): continue
    sp2 = sp_m[0].split(".")
    
    method = sp2[-1]
    cls = sp2[-2]
    package = ".".join(sp2[:-2])
    if(package != prev_package):
        if(prev_package != ""):
            print("}")
            print("")
        print("package " + package + ";")
        print("")
    if(prev_cls != cls):
        if(package == prev_package):
            print("}")
            print("")

        print("class "+ cls + " {")
    args = re.sub(r'\[L([^;]+);',r'\1 []',sp_m[1])
    perm = sp[1].replace(" and ", ", ").replace(" or ", ", ")
    print("\t@RequiredPremissions(\"%s\") public %s(%s;" % (perm, method, args))
    prev_cls = cls
    prev_package = package
    #print(package)
    #print(cls)
    #print(method)
print("}")