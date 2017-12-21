module javametamodel_withfield
// ABSTRACT SYNTAX

abstract sig Id {}

sig Package{} 

sig ClassId, MethodId,FieldId extends Id {}

abstract sig Accessibility {}

one sig public, private_, protected extends Accessibility {}

abstract sig Type {}

abstract sig PrimitiveType extends Type {}

one sig Int_, Long_ extends PrimitiveType {}

sig Class extends Type {
	package: one Package,
	id: one ClassId,
	extend: lone Class,
	methods: set Method,
	fields: set Field,
	implement: lone Class
} 

fact { 
	all c: Class | isInterface[c.implement]
}

fact { 
	all m: Method, c: Class | 
		m in c.methods && isAbstract[m] =>
          (isAbstract[c] || isInterface[c])
}

pred isAbstract[c: Class] {
	some m: c.methods | isAbstract[m]
}

pred isInterface[c: Class] {
	all m: c.methods | isAbstract[m]
}

fun classes[pack:Package]: set Class {
	pack.~package
}

sig Field {
    id : one FieldId,
    type: one Type,
    acc : lone Accessibility 
}

sig Method {
	id : one MethodId,
    param: lone Type,
    acc: lone Accessibility,
    return: one Type, 
    b: lone Body
} 

pred isAbstract[m:Method] {
	no m.b
}

abstract sig Body {}

sig LiteralValue extends Body {} // returns a random value

abstract sig Qualifier {}

one sig qthis_, this_, super_ extends Qualifier {}


sig MethodInvocation extends Body {
    id : one MethodId,
    q: lone Qualifier 
}
fact {
// call a declared method
    all mi:MethodInvocation | some m:Method | mi.id = m.id
// avoid recursive calls
    all m:Method | all mb: MethodInvocation | m.b = mb => mb.id != m.id    
}

//        return new A().k();
sig ConstructorMethodInvocation extends Body {
    idClass : one ClassId,
    idMethod: one MethodId
}
fact {
// calls a method declared in the class
    all ci: ConstructorMethodInvocation |
        some c:Class |
            ci.idClass = c.id && !isAbstract[c] && !isInterface[c] &&
            (some m:Method | m in c.methods && m.id = ci.idMethod && !isAbstract[m])  

//	all ci: ConstructorMethodInvocation |	some c: Class | 
//		(ci.idClass = c.id => (!isAbstract[c] && !isInterface[c]))

// avoid recursive calls
     all m:Method | all mb: ConstructorMethodInvocation | m.b = mb => mb.idMethod != m.id
}

fun classFromClassId[id1:ClassId]: set Class {
	id1.~id
}

fun fieldFromFieldId[id1:FieldId]: set Field {
	id1.~id
}

//        return x;
//        return this.x;
//        return super.x;
//        return A.this.x; -> implement in Java whether use this or qualified this
sig FieldInvocation extends Body {
    idField : one FieldId,
    qField: lone Qualifier
}

//        return new A().x;
sig ConstructorFieldInvocation extends Body {
    idClass2 : one ClassId,
    idField: one FieldId
}

fact {
    //calss a field declared in the class
    all ci: ConstructorFieldInvocation | 
        some c:Class |
            ci.idClass2 = c.id &&
           (some f:Field | f in c.fields && f.id = ci.idField)
}

// WELL-FORMED RULES
fact JavaWellFormedRules {
	noPackageContainsTwoClassesWithSameId[]
   noCalltoUndefinedField[]
    noSuperCallToNotInheritedField[]

    noClassExtendsItself[]
    allFieldsBelongToAClass[]
	noClassContainsTwoFieldsWithSameId []
	noClassContainsTwoMethodsWithSameSignature[]
	noClassExtendsAnotherWithSameId[]	
	allBodiesBelongToAMethod[]
    allMethodsBelongToAClass[]
    noSuperCallToNotInheritedMethod[]
    noCalltoUndefinedMethod[]


//MUDANCA ==============================
	abstractMethods[]
	noAbstractMethodInvocation[]
	allClassesMustImplementAbstractMethods[]
	allClassesMustImplementMethodsOfItsAnInterface[]
	allMethodsOfAnInterfaceArePublic[]
	allMethodsOfAnInterfaceAreAbstract[]
	aClassCannotImplementsItSelf[]
	aClassCannotImplementsAnotherClass[] 
	allFieldsOfAnInterfaceArePublic[]
//=====================================
	aClassCannotExtendAnInterface[]
	cannotReduceVisibilityofInheritedMethod[]
	methodCallsRules[]
	fieldCallsRules[]
}

pred aClassCannotExtendAnInterface[] {
	no c: Class | isInterface[c] && #c.~extend > 0
	no c: Class | isInterface[c] && #c.extend > 0
}

pred methodCallsRules[] {

no mfi: ConstructorMethodInvocation, m: Method | 	mfi.idMethod = m.id &&
	m.acc = protected && 
	(mfi.idClass.~id !in m.~methods.^extend) || 	(m.~methods !in mfi.idClass.~id.^extend)

no mi: MethodInvocation, m,m2: Method | mi.id = m2.id && m.b = mi && 
	m2.acc = private_ && 
	m.~methods != m2.~methods

all mi: MethodInvocation, m,m2: Method| 
	(mi.id = m2.id && m.b = mi && 
	#m2.acc = 0 &&
	 m.~methods != m2.~methods && 
	 m2.~methods in m.~methods.^extend) => 	(m.~methods.^extend.package = 
	m2.~methods.package &&
	m.~methods.package = m2.~methods.package)

}

pred fieldCallsRules[] {

no cfi: ConstructorFieldInvocation, f: Field | 	cfi.idField = f.id &&
	f.acc = protected && 
	(cfi.idClass2.~id !in f.~fields.^extend) || 	(f.~fields !in cfi.idClass2.~id.^extend)

no fi: FieldInvocation, m: Method, f: Field |  fi.idField = f.id && m.b = fi && 
	f.acc = private_ && 
	m.~methods != f.~fields


all fi: FieldInvocation, m: Method, f: Field | 
	(fi.idField = f.id && m.b = fi && 
	#f.acc = 0 &&
	 m.~methods != f.~fields && 
	 f.~fields in m.~methods.^extend) => 	(m.~methods.^extend.package = 
	f.~fields.package &&
	m.~methods.package = f.~fields.package)

}

pred cannotReduceVisibilityofInheritedMethod[] {
	all m, m2: Method | 
			(m in m2.~methods.^extend.methods && m.id = m2.id && m.param = m2.param) =>
						(m.acc = m2.acc && m.acc = public)
} 

pred noClassContainsTwoMethodsWithSameSignature[] {
	all c: Class | all m1,m2:c.methods | m1!=m2 =>(m1.id != m2.id or m1.param != m2.param)
}

pred noSuperCallToNotInheritedField[] {
  all fi:FieldInvocation | 
    fi.qField = super_ => 
      some disj c1,c2: Class, m1:c1.methods, f:c2.fields |  fi in m1.b && fi.idField = f.id && c2 in c1.^extend && f.acc != private_

 
}
pred noCalltoUndefinedField[] {
 all mi:FieldInvocation | 
     ( mi.qField = this_) => 
          some c1,c2: Class, m1:c1.methods, f:c2.fields | mi in m1.b && mi.idField = f.id && ((c1 = c2) || ((c2 in c1.^extend) && (f.acc != private_)))

  all mi:FieldInvocation | 
      ( mi.qField = qthis_) => 
          some c1,c2: Class, m1:c1.methods, f:c2.fields | mi in m1.b && mi.idField = f.id && ((c1 = c2) || ((c2 in c1.^extend) && (f.acc != private_)))

  all mi:FieldInvocation | 
      ( #mi.qField = 0) => 
          some c1,c2: Class, m1:c1.methods, f:c2.fields | mi in m1.b && mi.idField = f.id && ((c1 = c2) || ((c2 in c1.^extend) && (f.acc != private_)))
}

pred noPackageContainsTwoClassesWithSameId[] {
	all package: Package | all c1,c2:classes[package] | c1!=c2 => c1.id != c2.id
}

pred noClassExtendsItself[] {
	no c:Class | c in c.^extend
	no c:Class | c in c.^implement
	no c1,c2: Class | c1 in c2.^extend && c2 in c1.^implement
	
}

pred noClassExtendsAnotherWithSameId[] {
	all c1:Class | no c2: c1.^extend | c1.id = c2.id
}

pred noClassContainsTwoFieldsWithSameId [] {
    no c:Class | some disj f1,f2:Field | f1.id = f2.id && f1 + f2 in c.fields
}

pred noCalltoUndefinedMethod[] {
  all mi:MethodInvocation | 
      (#mi.q = 0 || mi.q = this_) => 
          some c1,c2: Class, m1:c1.methods, m2:c2.methods  | mi in m1.b && mi.id = m2.id && ((c1 = c2) || ((c2 in c1.^extend) && (m2.acc != private_)))

  all mi:MethodInvocation | 
      (mi.q = qthis_) => 
          some c1:Class, m1,m2:c1.methods | mi in m1.b && mi.id = m2.id 


}

pred noSuperCallToNotInheritedMethod[] {
   all mi:MethodInvocation | 
       mi.q = super_ => 
          some c1,c2: Class, m1:c1.methods, m2:c2.methods |  mi in m1.b && mi.id = m2.id && c2 in c1.^extend && (m2.acc != private_)
}


pred allFieldsBelongToAClass [] {
    all f:Field | one c:Class | f in c.fields
}

pred allMethodsBelongToAClass [] {
    all m:Method | one c:Class | m in c.methods
}

pred allBodiesBelongToAMethod [] {
   Body in Method.b
}

pred aClassCannotImplementsItSelf[] {
	no c: Class | c.id = c.implement.id
}

pred aClassCannotImplementsAnotherClass[] {
	no c: Class | some c1: Class | !isInterface[c1] && c1 in c.implement
}

pred allMethodsOfAnInterfaceArePublic[] {
	all c: Class | isInterface[c] && some c.methods => (c.methods.acc = public)
	no m: Method | m.acc = private_ && isAbstract[m]
}

pred allFieldsOfAnInterfaceArePublic[] {
	all c: Class | isInterface[c] && some c.fields => (c.fields.acc = public)
}


pred allMethodsOfAnInterfaceAreAbstract[] {
	all c: Class | isInterface[c] => 
		all m: c.methods |	 isAbstract[m]
}


pred allClassesMustImplementAbstractMethods[] {
	all c: Class | (isAbstract[c.^extend] && !isAbstract[c]) =>  (all m: Method | some m2: Method | 
	(isAbstract[m] && m in c.^extend.methods) => (m2.id = m.id && m2.param = m.param && m2.acc = m.acc &&
             m2 in c.methods && !isAbstract[m2]))
	
	all m: Method, c1,c2: Class | some m2: Method |
		 (c1 in c2.^extend && isAbstract[c1] && !isAbstract[c2] && isAbstract[m] 
				&& m in c1.methods) => (m2 in c2.methods && !isAbstract[m2] && m2.id = m.id && m2.param = m.param && m2.acc = m.acc )

}

pred allClassesMustImplementMethodsOfItsAnInterface[] {

	all c: Class | (#c.implement = 1 && !isAbstract[c]) =>  
		(all m: Method | some m2: Method | 
			(m in c.implement.methods) => 
				(m2.id = m.id && m2.param = m.param && m2.acc = m.acc && 
				 m2 in c.methods && !isAbstract[m2]))
}

pred noAbstractMethodInvocation[] {
    no m: Method | some mi: MethodInvocation  | mi.id = m.id && isAbstract[m]
}


pred abstractMethods[] {

	all m: Method | isAbstract[m] =>
		((some c: Class | m in c.methods && (isAbstract[c] || isInterface[c])) )

	all c: Class | isAbstract[c] && some c.methods => (some m: Method | m in c.methods && isAbstract[m])

}

pred noInterfaceExtendsAClass[] {
	all c: Class | (isInterface[c] || isAbstract[c]) => (#c.extend = 0 && #c.implement = 0)
}


pred aClassCannotExtendAClassAndImplementAnInterface[] {
	no c: Class | #c.extend > 0 && #c.implement >0
}


pred oneBodyPerMethod[] {
	no m1,m2:Method | m1!= m2 && m1.b = m2.b && some m1.b
}

pred paramMustBePrimitive[] {
	all m: Method | (#m.param = 1) => (m.param in Int_ + Long_)
}


pred doNotGenerateEmptyPackage[] {
	Package in Class.package 
}


pred onlyGenerateIdsWhenThereIsAnEntity[] {
	all id1:ClassId | some c:Class | c.id = id1 
	all id1:MethodId| some m:Method | m.id = id1
	FieldId in Field.id
}

//daqui pra baixo, os predicados sao opcionais
pred atLeastTwoPackages[] {
#Package > 1
}

pred differentIdsForFields[] {
  some disj f1,f2:Field | class[f1] != class[f2]
Field.type = Int_
#FieldId = 2
}


pred overloading[t1:Type] {
  one disj m1,m:Method | m1.id = m.id && #m1.param = 0 && m.param = t1 && m1.b + m.b in LiteralValue && sameHierarchy[m1,m]
}

pred twoDistinctFields {
some disj f1,f2:Field | f1.id != f2.id && f1.type + f2.type in Int_
}

//THE FOLLOWING PREDICATES ARE USED TO TEST SOME REFACTORING IMPLEMENTATIONS
pred someCaller[] {
  one m: Method | m.acc = public  && #m.param = 0 && m.b !in LiteralValue 
}

pred differentIdsForMethods[] {
  all disj m1,m2:Method | m1.id != m2.id
}

pred returnMethodCall[m:Method] {
m.b in MethodInvocation || m.b in ConstructorMethodInvocation
}

pred someTester[id1: MethodId] {
one m: Method | m.acc = public  && #m.param = 0 && m.b in MethodInvocation && #m.b.q = 0  && m.b.id = id1
}

pred someFieldsWithDiffIds[] {
  some disj f1,f2:Field | class[f1] != class[f2]
Field.type = Int_
#FieldId = 2
}

pred differentIdsForClasses[] {
all cid:ClassId | one c:Class | c.id = cid
}

pred somePrimitiveFields[] {
  some disj f1,f2:Field | class[f1] != class[f2]
Field.type = Int_
#FieldId = 2
}

pred someMain[] {
  one m: Method | m.acc = public  && #m.param = 0 && m.b !in LiteralValue 
}

pred someFieldhiding[] {
some disj f1,f2:Field | f1.id = f2.id && f1.type + f2.type in Int_ //&& sameHierarchy[f1,f2]
}

pred someInheritance[]  {
  some disj c1,c2:Class | c1 in c2.extend
}

pred caller[m:Method] {
  m.acc = public  && #m.param = 0 && m.b !in LiteralValue //&& methodNotCalled[m] 
}

pred methodMain[id1: MethodId] {
one m: Method | m.acc = public  && #m.param = 0 && m.b in MethodInvocation && #m.b.q = 0  && m.b.id = id1
&& methodNotCalled[m] 
}

pred methodNotCalled[m1:Method] {
no mi:MethodInvocation| mi.id = m1.id 
no cmi:ConstructorMethodInvocation | cmi.idMethod = m1.id
}


pred oneOverriding[] {
  one disj m1,m :Method | m1.id = m.id && #m1.param  = 0 && #m.param = 0 && m1.b + m.b in LiteralValue && sameHierarchy[m1,m]
}

pred overloading[t1:Type,t2:Type] {
  one disj m1,m:Method | m1.id = m.id && m1.param = t1 && m.param = t2 && m1.b + m.b in LiteralValue && sameHierarchy[m1,m]
}
pred overloading[t1:Type] {
  one disj m1,m:Method | m1.id = m.id && #m1.param = 0 && m.param = t1 && m1.b + m.b in LiteralValue && sameHierarchy[m1,m]
}

pred overloading[i1:Int,i2:Int] {
  one disj m1,m:Method | m1.id = m.id && #m1.param = i1 && #m.param = i2 && m1.param != m.param && m1.b + m.b in LiteralValue && sameHierarchy[m1,m]
}

pred sameDirectlyHierarchy[m1,m2:Method] {
   some c1,c2 : Class | m1 + m2 in c1.methods || (m1 in c1.methods && c1 in c2.extend && m2 in c2.methods)
}

pred sameHierarchy[m1,m2:Method] {
   some c1,c2 : Class | m1 + m2 in c1.methods || (m1 in c1.methods && c1 in c2.^extend && m2 in c2.methods)
}

pred returnMethodCall[m:Method] {
m.b in MethodInvocation || m.b in ConstructorMethodInvocation
}

pred someOverriding[] {
  some disj m1,m :Method | m1.id = m.id && m1.param  = Int_ && m.param = Int_ && m1.b + m.b in LiteralValue && sameHierarchy[m1,m]
}

pred someOverridingDirectlyHierarchy[] {
  some disj m1,m :Method | m1.id = m.id && m1.param  = Int_ && m.param = Int_ && m1.b + m.b in LiteralValue && sameDirectlyHierarchy[m1,m]
}

pred oneOverloading[t1:Type,t2:Type] {
  one disj m1,m:Method | m1.id = m.id && m1.param = t1 && m.param = t2 && m1.b + m.b in LiteralValue && sameHierarchy[m1,m]
}

pred oneOverloadingDirectlyHierarchy[t1:Type,t2:Type] {
  one disj m1,m:Method | m1.id = m.id && m1.param = t1 && m.param = t2 && m1.b + m.b in LiteralValue && sameDirectlyHierarchy[m1,m]
}

pred oneOverloading[i1:Int,i2:Int] {
one disj m1,m2:Method | m1.id = m2.id && #m1.param = i1 && #m2.param = i2 && m1.param != m2.param && m1.param + m2.param !in Class && m1.b + m2.b in LiteralValue && sameHierarchy[m1,m2]
}

pred isSimpleValueBody[m:Method] {
   m.b in LiteralValue
}

pred isMain[m:Method] {
  m.acc = public  && #m.param = 0 && m.b !in LiteralValue 
}

pred isCaller[m:Method] {
  m.acc = public  && #m.param = 0 && m.b !in LiteralValue 
}

pred someMain[id1: MethodId] {
  one m: Method | m.acc = public  && #m.param = 0 && m.b in MethodInvocation && #m.b.q = 0  && m.b.id = id1  && methodNotCalled[m] 
}

pred oneOverridingDirectlyHierarchy[] {
  one disj m1,m :Method | m1.id = m.id && #m1.param  = 0 && #m.param = 0 && m1.b + m.b in LiteralValue && sameDirectlyHierarchy[m1,m]
}

//AUXILIAR FUNCTIONS
fun class[f1:Field]: one Class {
	f1.~fields
}

fun class[m:Method]: one Class {
	m.~methods
}

pred returnTypeMustBeLong[] {
	all m:Method | m.return = Long_
}

pred show[] {
//optimizations
	returnTypeMustBeLong[]
	paramMustBePrimitive[]
	doNotGenerateEmptyPackage[]
	onlyGenerateIdsWhenThereIsAnEntity[]
	aClassCannotExtendAClassAndImplementAnInterface[]
	oneBodyPerMethod[]
}

//numero de classes, metodos, e atributos 
assert test1 {
#Package >= 0
#Class >= 0 
#Method >= 0
}



//corpo de metodo
assert test2 {
#LiteralValue >= 0
}

//visibilidade
assert test3 {
all m : Method | m.acc = public || m.acc = protected || m.acc = private_ || #m.acc = 0
}

//ids
assert test4 {
all mid:MethodId | some m:Method | m.id = mid
all cid:ClassId | some c:Class | c.id = cid
all id1:FieldId | some f:Field | f.id = id1
}

assert test5 {
no c1,c2: Class | c1 != c2 && c1.package = c2.package && c1.id = c2.id
}

assert TesteRohit {
//	all c: Class | isInterface[c] => !isAbstract[c]
	all c: Class | isAbstract[c] => !isInterface[c]
}
check TesteRohit for 3

//run show for 7
//check test1 for 7
//check test2 for 7
//check test3 for 7
//check test4 for 7
//check test5 for 7



