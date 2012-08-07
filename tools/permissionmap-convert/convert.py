#!/usr/bin/python
# usage: python convert.py
import re

print("import sparta.checkers.quals.*;")
print

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
    args = args.replace("[B", "boolean []").replace("[J", "long []").replace("[F", "float []").replace("[Ljava.lang.String", "String []")
    perm = sp[1].replace(" and ", ", ").replace(" or ", ", ").replace("android.","android.Manifest.").replace(", NONE","").replace(" AND ", ", ").replace(" OR ", ", ")
    args_arr = args.split(")")[0].strip().split(",")
    method = method.replace("<init>", cls)
    lst = []
    cnt = 0
    for arg in args_arr:
        if(arg == ""): continue
        lst.append(arg + " p"+str(cnt))
        cnt += 1
    print("\t@RequiredPermissions({%s}) Object %s(%s);" % (perm, method, ",".join(lst)))
    prev_cls = cls
    prev_package = package
    #print(package)
    #print(cls)
    #print(method)
print("}")
