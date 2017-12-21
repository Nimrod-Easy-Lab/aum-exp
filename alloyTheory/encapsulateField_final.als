module encapsulate_field

open javametamodel_withfield_final


run show for 2 Package, 2 Class, 1 Field, 4 Method, 2 ClassId, 4 MethodId, 1 FieldId, 4 Body, 0 FieldInvocation, 0 ConstructorFieldInvocation 


one sig getfieldid, caller extends MethodId {}

fact Encapsulate {

//geral
some Field

somePublicField[] 

differentIdsForClasses[]
differentIdsForMethods[]
#Package = 2
someTester[caller]
someGetter[]

no c: Class | !isInterface[c] && #c.methods =0
some c: Class | isAbstract[c] && #c.methods > 0
some c: Class |  !isInterface[c] &&  !isAbstract[c] 
no c1,c2:Class | c1 != c2 && c1.id = c2.id
}

pred someGetter[] {
  one m:Method | m.b in LiteralValue && #m.param = 0 && m.id = getfieldid 
}

pred somePublicField[] {
  some f:Field| f.type = Long_ 
}




