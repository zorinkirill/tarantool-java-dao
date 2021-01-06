package com.kappadrive.dao.gen.util;

import com.kappadrive.dao.api.TarantoolDao;
import com.kappadrive.dao.api.Tuple;
import com.kappadrive.dao.gen.DaoImplData;
import com.kappadrive.dao.gen.DaoMethodData;
import com.kappadrive.dao.gen.EntityData;
import com.kappadrive.dao.gen.FieldData;
import com.kappadrive.dao.gen.GenerateDaoProcessor;
import com.kappadrive.dao.gen.tuple.TupleVisitor;
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.WildcardTypeName;

import javax.annotation.Nonnull;
import javax.annotation.processing.Generated;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.ExecutableType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiFunction;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public final class GenerateUtil {

    public static final String TUPLE = "tuple";
    public static final String ENTITY = "entity";
    public static final String SPACE = "space";
    public static final String CLIENT = "client";
    public static final ParameterizedTypeName OBJECT_LIST_TYPE = ParameterizedTypeName.get(ClassName.get(List.class), TypeName.get(Object.class));
    public static final ParameterizedTypeName ANY_LIST_TYPE = ParameterizedTypeName.get(ClassName.get(List.class), WildcardTypeName.subtypeOf(TypeName.get(Object.class)));
    public static final ParameterizedTypeName LIST_LIST_OBJECT_TYPE = ParameterizedTypeName.get(ClassName.get(List.class), ParameterizedTypeName.get(List.class, Object.class));

    private final ProcessingEnvironment processingEnv;
    private final TypeElement objectType;

    public GenerateUtil(ProcessingEnvironment processingEnv) {
        this.processingEnv = processingEnv;
        objectType = processingEnv.getElementUtils().getTypeElement(Object.class.getCanonicalName());
    }

    @Nonnull
    public static AnnotationSpec createGeneratedAnnotation() {
        return AnnotationSpec.builder(Generated.class)
                .addMember("value", "$S", GenerateDaoProcessor.class.getCanonicalName())
                .addMember("date", "$S", DateTimeFormatter.ISO_DATE_TIME.format(LocalDateTime.now()))
                .build();
    }

    @Nonnull
    public <T> Optional<T> lookupStyleValue(
            @Nonnull final TypeElement element,
            @Nonnull final BiFunction<? super Element, Class<? extends Annotation>, Optional<T>> function
    ) {
        PackageElement pkg = processingEnv.getElementUtils().getPackageOf(element);
        return function.apply(element, TarantoolDao.Style.class)
                .or(() -> function.apply(pkg, TarantoolDao.Style.class));
    }

    public void writeSafe(@Nonnull final JavaFile javaFile) {
        try {
            javaFile.writeTo(processingEnv.getFiler());
        } catch (IOException e) {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "Error writing java file: " + e.getMessage());
        }
    }

    @Nonnull
    public Collection<ExecutableElement> getAllAbstractMethods(@Nonnull final TypeElement typeElement) {
        return processingEnv.getElementUtils().getAllMembers(typeElement)
                .stream()
                .filter(e -> !objectType.equals(e.getEnclosingElement()))
                .filter(e -> e.getKind() == ElementKind.METHOD)
                .map(e -> (ExecutableElement) e)
                .filter(e -> !e.isDefault())
                .filter(e -> !e.getModifiers().contains(Modifier.STATIC))
                .collect(Collectors.toList());
    }

    @Nonnull
    public List<? extends TypeMirror> resolveGenericTypes(
            @Nonnull final ExecutableElement executableElement,
            @Nonnull final DeclaredType type
    ) {
        ExecutableType executableType = (ExecutableType) processingEnv.getTypeUtils().asMemberOf(type, executableElement);
        return executableType.getParameterTypes();
    }

    @Nonnull
    public List<FieldData> getAllFields(@Nonnull final DeclaredType declaredType) {
        AtomicInteger order = new AtomicInteger(0);
        return declaredType.asElement()
                .getEnclosedElements()
                .stream()
                .filter(e -> e.getKind() == ElementKind.FIELD)
                .map(e -> (VariableElement) e)
                .filter(e -> AnnotationUtil.getAnnotationMirror(e, Tuple.Ignore.class).isEmpty())
                .map(field -> createFieldMeta(field, order, declaredType))
                .collect(Collectors.toList());
    }

    public static void verifyFields(@Nonnull final List<FieldData> fields, @Nonnull final DeclaredType entityType) {
        if (fields.stream().map(FieldData::getOrder).distinct().count() != fields.size()) {
            throw new IllegalArgumentException("Should be no duplicates between fields for @" + Tuple.Order.class.getSimpleName() + " in: " + entityType);
        }
        if (fields.stream().map(FieldData::getOrder).anyMatch(o -> o < 0 || o >= fields.size())) {
            throw new IllegalArgumentException(
                    String.format("All values for @%s should be in bounds [%d:%d] in: %s ", Tuple.Order.class.getSimpleName(), 1, fields.size(), entityType));
        }
        if (fields.stream().noneMatch(FieldData::isKey)) {
            throw new IllegalArgumentException("At least one field should be annotated with @" + Tuple.Key.class.getSimpleName() + " in: " + entityType);
        }
    }

    @Nonnull
    public static String capitalize(@Nonnull final String string) {
        return string.substring(0, 1).toUpperCase() + string.substring(1);
    }

    @Nonnull
    public FieldData createFieldMeta(@Nonnull final VariableElement field,
                                     @Nonnull final AtomicInteger order,
                                     @Nonnull final DeclaredType declaredType) {
        String name = field.getSimpleName().toString();
        Optional<Integer> orderValue = AnnotationUtil.getAnnotationValue(field, Tuple.Order.class, "value", Integer.class);
        if (orderValue.isPresent() && order.get() != 0) {
            throw new IllegalStateException("Either no fields or all not ignored fields should be marked with @" + Tuple.Order.class + " in: " + declaredType);
        }
        return FieldData.builder()
                .field(field)
                .name(name)
                .type(field.asType())
                .getter("get" + capitalize(field.getSimpleName().toString()))
                .setter("set" + capitalize(field.getSimpleName().toString()))
                .key(AnnotationUtil.getAnnotationMirror(field, Tuple.Key.class).isPresent())
                .order(orderValue.map(i -> i - 1).orElseGet(order::getAndIncrement))
                .build();
    }

    @Nonnull
    public MethodSpec.Builder methodBuilder(@Nonnull final ExecutableElement executableElement, @Nonnull final DeclaredType interfaceType) {
        Types types = processingEnv.getTypeUtils();
        MethodSpec.Builder builder = MethodSpec.overriding(executableElement, interfaceType, types)
                .addAnnotations(AnnotationUtil.getAnnotationSpecs(executableElement, Override.class));
        List<? extends TypeMirror> resolvedParameterTypes = resolveGenericTypes(executableElement, interfaceType);
        List<? extends VariableElement> declaredParameters = executableElement.getParameters();
        for (int i = 0, size = builder.parameters.size(); i < size; i++) {
            ParameterSpec parameter = builder.parameters.get(i);
            TypeName type = TypeName.get(resolvedParameterTypes.get(i));
            VariableElement declared = declaredParameters.get(i);
            ParameterSpec.Builder parameterBuilder = ParameterSpec.builder(type, declared.getSimpleName().toString());
            parameterBuilder.modifiers.addAll(parameter.modifiers);
            parameterBuilder.annotations.addAll(AnnotationUtil.getAnnotationSpecs(declared));
            builder.parameters.set(i, parameterBuilder.build());
        }
        return builder;
    }

    @Nonnull
    public TypeMirror getFinalType(@Nonnull final TypeMirror typeMirror) {
        if (isAssignableGeneric(typeMirror, Optional.class)) {
            return getGenericType(typeMirror, 0);
        }
        return typeMirror;
    }

    @Nonnull
    private TypeMirror getGenericType(@Nonnull final TypeMirror typeMirror, final int index) {
        return ((DeclaredType) typeMirror).getTypeArguments().get(index);
    }

    public boolean isEnum(@Nonnull final TypeMirror typeMirror) {
        return ((DeclaredType) typeMirror).asElement().getKind() == ElementKind.ENUM;
    }

    public boolean hasType(@Nonnull final TypeMirror typeMirror, @Nonnull final Class<?> typeClass) {
        return processingEnv.getTypeUtils().isSameType(
                typeMirror,
                processingEnv.getElementUtils().getTypeElement(typeClass.getCanonicalName()).asType());
    }

    public boolean isSubType(@Nonnull final TypeMirror typeMirror, @Nonnull final Class<?> typeClass) {
        return processingEnv.getTypeUtils().isSubtype(
                typeMirror,
                processingEnv.getElementUtils().getTypeElement(typeClass.getCanonicalName()).asType());
    }

    public boolean isAssignable(@Nonnull final TypeMirror typeMirror, @Nonnull final Class<?> typeClass) {
        return processingEnv.getTypeUtils().isAssignable(
                typeMirror,
                processingEnv.getElementUtils().getTypeElement(typeClass.getCanonicalName()).asType());
    }

    public boolean isAssignableGeneric(@Nonnull final TypeMirror typeMirror, @Nonnull final Class<?> typeClass) {
        return processingEnv.getTypeUtils().isAssignable(
                processingEnv.getTypeUtils().erasure(typeMirror),
                processingEnv.getElementUtils().getTypeElement(typeClass.getCanonicalName()).asType());
    }

    @Nonnull
    public MethodSpec createDaoToTupleMethod(
            @Nonnull final String name,
            @Nonnull final EntityData entityData,
            @Nonnull final Predicate<? super FieldData> fieldsFilter
    ) {
        String getters = entityData.fieldsSortedByOrder()
                .filter(fieldsFilter)
                .map(f -> TupleVisitor.visit(f.getType(), this, t -> t.createEntityGetter(f, ENTITY)))
                .collect(Collectors.joining(", "));
        return MethodSpec.methodBuilder(name)
                .addModifiers(Modifier.PRIVATE, Modifier.STATIC)
                .addAnnotation(Nonnull.class)
                .addParameter(ParameterSpec.builder(TypeName.get(entityData.getType()), ENTITY, Modifier.FINAL)
                        .addAnnotation(Nonnull.class)
                        .build())
                .returns(OBJECT_LIST_TYPE)
                .addStatement("return $T.asList($L)", Arrays.class, getters)
                .build();
    }

    @Nonnull
    public MethodSpec createDaoToEntityMethod(@Nonnull final EntityData entityData) {
        MethodSpec.Builder builder = MethodSpec.methodBuilder("toEntity")
                .addModifiers(Modifier.PRIVATE, Modifier.STATIC)
                .addAnnotation(Nonnull.class)
                .addParameter(ParameterSpec.builder(ANY_LIST_TYPE, TUPLE, Modifier.FINAL)
                        .addAnnotation(Nonnull.class)
                        .build())
                .returns(TypeName.get(entityData.getType()))
                .addStatement("final var $L = new $T()", ENTITY, entityData.getType());
        entityData.fieldsSortedByOrder()
                .forEach(f -> builder.addStatement(TupleVisitor.visit(f.getType(), this, t -> t.createEntitySetter(f))));
        return builder
                .addStatement("return $L", ENTITY)
                .build();
    }

    @Nonnull
    public List<DaoMethodData> getAllDaoMethods(@Nonnull final TypeElement type) {
        return getAllAbstractMethods(type).stream()
                .map(this::createDaoMethodData)
                .collect(Collectors.toList());
    }

    @Nonnull
    public DaoMethodData createDaoMethodData(@Nonnull final ExecutableElement method) {
        return DaoMethodData.builder()
                .method(method)
                .build();
    }

    public boolean hasSingleParameter(
            @Nonnull ExecutableElement executableElement,
            @Nonnull DaoImplData daoImplData,
            @Nonnull DeclaredType type
    ) {
        return executableElement.getParameters().size() == 1
                && processingEnv.getTypeUtils().isSubtype(
                resolveGenericTypes(executableElement, daoImplData.getInterfaceType()).get(0), type);
    }
}
