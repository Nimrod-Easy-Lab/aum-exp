module example

open javametamodel_nofield

run show  for 7 Id, exactly 2 Package, 3 Class, 3 Method,  exactly 3 ClassId, 2 MethodId, 7 Body

one sig M extends MethodId{}

fact Pullup {

some c:Class | someSuperClass[c] && someMethod[c] 


no c: Class | !isInterface[c] && #c.methods =0
some c: Class | isAbstract[c] && #c.methods > 0
some c: Class |  !isInterface[c] &&  !isAbstract[c] 
no c1,c2:Class | c1 != c2 && c1.id = c2.id

}

pred someSuperClass[c:Class] {
some c2:Class | c2 in c.extend || c2 in c.implement
}

pred someMethod[c:Class] {
 some m:Method | m in c.methods && m.id = M
&& (!isAbstract[m] => someMain[m.id] &&  isCaller[m]) 
}







