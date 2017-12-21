open javametamodel_withfield

fact{
    all m:Method | m.acc = public
}

run show for 1 Package, 1 Class, 1 Field, 1 Method, 1 ClassId, 1 Body, 1 FieldId, 1 MethodId