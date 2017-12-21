module pushdownfield

open javametamodel_withfield_final


run show for  2 Package, 3 Class, 2 Field, 2 Method, 7 Id, 2 Body

fact PullUp {
#Package = 2

some c1,c2:Class, f:Field |( c1 in c2.extend || c1 in c2.implement) && f in c2.fields && c2.id = ID1 

differentIdsForClasses[]
someFieldhiding[]
someMain[]

no c: Class | !isInterface[c] && #c.methods =0
some c: Class |  !isInterface[c] &&  !isAbstract[c] 
no c1,c2:Class | c1 != c2 && c1.id = c2.id


}
one sig ID1 extends ClassId {}




