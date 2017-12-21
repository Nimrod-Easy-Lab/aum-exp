module renameclass

open javametamodel_withfield_final

run show for 2 Package, 3 Class, 3 Method, 6 Id, 3 Body, 0 Field, 0 FieldId, 0 FieldInvocation, 0 ConstructorFieldInvocation

fact RenameClass {

//Main constraint//
some Class

//Additional constraint//
atLeastTwoPackages[]
//someOverloading[Int_,Long_] || someOverriding[]
oneOverloadingDirectlyHierarchy[Int_,Long_] || oneOverridingDirectlyHierarchy[]
differentIdsForClasses[]
someMain[]
someInheritance[]
}



