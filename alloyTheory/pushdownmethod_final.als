module example
open javametamodel_nofield

run show for exactly 2 Package, exactly 3 Class, exactly 4 Method,  exactly 3 ClassId, exactly 3 MethodId, 8 Id, 4 Body//, 0 Field




//geral, nome do metodo refatorado
one sig M extends MethodId {}


fact PushDown{

//push down
some c:Class| someMethod[c] && ((someSubClass[c] && !isAbstract[c]) || (isAbstract[c]))

//additional constraints
oneOverloading[1,1] || oneOverriding[]

no c: Class | !isInterface[c] && #c.methods =0
some c: Class | isAbstract[c] && #c.methods > 0
some c: Class |  !isInterface[c] &&  !isAbstract[c] 
no c1,c2:Class | c1 != c2 && c1.id = c2.id
}


pred someSubClass[c:Class] {
some c2:Class | c in c2.extend

}

pred someMethod[c:Class] {
 some m:Method | m in c.methods && m.id = M
&& (!isAbstract[m] => someMain[m.id] &&  isCaller[m]) 

}









