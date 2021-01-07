package com.kappadrive.dao.gen;

import com.kappadrive.dao.api.Delete;
import com.kappadrive.dao.api.Insert;
import com.kappadrive.dao.api.Operation;
import com.kappadrive.dao.api.Replace;
import com.kappadrive.dao.api.Select;
import com.kappadrive.dao.api.Update;
import com.kappadrive.dao.api.Upsert;
import com.kappadrive.dao.gen.tuple.TupleUtil;
import com.kappadrive.dao.gen.util.AnnotationUtil;
import com.kappadrive.dao.gen.util.GenerateUtil;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.MethodSpec;
import lombok.RequiredArgsConstructor;
import org.tarantool.Iterator;

import javax.annotation.Nonnull;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.kappadrive.dao.gen.util.AnnotationUtil.isAnnotatedWith;
import static com.kappadrive.dao.gen.util.GenerateUtil.CLIENT;
import static com.kappadrive.dao.gen.util.GenerateUtil.LIST_LIST_OBJECT_TYPE;
import static com.kappadrive.dao.gen.util.GenerateUtil.SPACE;
import static java.util.function.Predicate.not;
import static java.util.stream.Collectors.joining;

@RequiredArgsConstructor
public enum DaoMethodType {

    SELECT(List.of("find", "select"), Select.class) {
        @Nonnull
        @Override
        public MethodSpec createMethodSpec(@Nonnull DaoMethodData method, @Nonnull DaoImplData daoImplData, @Nonnull GenerateUtil generateUtil) {
            String index = AnnotationUtil.getAnnotationValue(method.getMethod(), Select.Index.class, String.class)
                    .orElseThrow(() -> new IllegalStateException("Select method should be annotation with @" + Select.Index.class.getSimpleName() + ": " + method.getMethod()));
            List<? extends VariableElement> params = method.getMethod().getParameters();

            List<? extends VariableElement> selectParams = params.stream()
                    .filter(not(isAnnotatedWith(Select.Offset.class, Select.Limit.class)))
                    .collect(Collectors.toList());
            String args = getArgsAsString(method, selectParams, daoImplData, generateUtil);
            String iterator = AnnotationUtil.getAnnotationEnumValue(method.getMethod(), Select.class, "value", Select.Iterator.class)
                    .filter(i -> i != Select.Iterator.AUTO)
                    .map(Enum::toString)
                    .orElse(args.isEmpty() ? "ALL" : "EQ");
            MethodSpec.Builder builder = generateUtil.methodBuilder(method.getMethod(), daoImplData.getInterfaceType())
                    .addStatement("final var result = ($T) $L.syncOps().select($L, $S, $T.asList(" + args + "), $L, $L, $T.$L)",
                            LIST_LIST_OBJECT_TYPE, CLIENT, SPACE, index, Arrays.class,
                            lookupUniqueIntParameter(method, params, Select.Offset.class).orElse(0),
                            lookupUniqueIntParameter(method, params, Select.Limit.class).orElse(Integer.MAX_VALUE),
                            Iterator.class, iterator);
            TypeMirror returnType = method.getMethod().getReturnType();
            if (isSingular(generateUtil, returnType)) {
                addReturnOptional(daoImplData, builder);
            } else {
                builder.addStatement("return result.stream().map($L::$N).collect($T.toList())", daoImplData.getImplName(), daoImplData.getToEntity(), Collectors.class);
            }
            return builder.build();
        }

        @Nonnull
        private Optional<Object> lookupUniqueIntParameter(
                @Nonnull final DaoMethodData method,
                @Nonnull final List<? extends VariableElement> params,
                @Nonnull final Class<? extends Annotation> annotation
        ) {
            List<? extends VariableElement> result = params.stream().filter(isAnnotatedWith(annotation))
                    .collect(Collectors.toList());
            if (result.size() > 1) {
                throw new IllegalStateException("Select method can't have more than 1 @" + annotation.getSimpleName() + " parameter: " + method.getMethod());
            }
            if (result.isEmpty()) {
                return Optional.empty();
            }
            if (result.get(0).asType().getKind() != TypeKind.INT) {
                throw new IllegalStateException("Select @" + annotation.getSimpleName() + " parameter should have type int: " + method.getMethod());
            }
            return Optional.of(result.get(0).getSimpleName());
        }

        private boolean isSingular(@Nonnull final GenerateUtil generateUtil, @Nonnull final TypeMirror returnType) {
            if (generateUtil.isAssignableGeneric(returnType, List.class)
                    || generateUtil.isAssignableGeneric(returnType, Collection.class)
                    || generateUtil.isAssignableGeneric(returnType, Iterable.class)) {
                return false;
            }
            if (!generateUtil.isAssignableGeneric(returnType, Optional.class)) {
                throw new IllegalArgumentException("Select/Find method return type should be Optional or List/Collection/Iterable");
            }
            return true;
        }
    },
    INSERT(List.of("insert"), Insert.class) {
        @Nonnull
        @Override
        public MethodSpec createMethodSpec(@Nonnull DaoMethodData method, @Nonnull DaoImplData daoImplData, @Nonnull GenerateUtil generateUtil) {
            if (!generateUtil.hasSingleParameter(method.getMethod(), daoImplData, daoImplData.getEntity().getType())) {
                throw new IllegalStateException("Insert method should have only single entity parameter: " + method.getMethod());
            }
            MethodSpec.Builder builder = generateUtil.methodBuilder(method.getMethod(), daoImplData.getInterfaceType());
            CodeBlock.Builder operation = CodeBlock.builder();
            if (!isVoid(method)) {
                operation.add("final var result = ($T) ", LIST_LIST_OBJECT_TYPE);
            }
            operation.add("$L.syncOps().insert($L, $N($L))", CLIENT, SPACE, daoImplData.getToTuple(), method.getMethod().getParameters().get(0).getSimpleName());
            builder.addStatement(operation.build());
            if (!isVoid(method)) {
                addReturnEntity(daoImplData, builder);
            }
            return builder.build();
        }
    },
    REPLACE(List.of("replace"), Replace.class) {
        @Nonnull
        @Override
        public MethodSpec createMethodSpec(@Nonnull DaoMethodData method, @Nonnull DaoImplData daoImplData, @Nonnull GenerateUtil generateUtil) {
            if (!generateUtil.hasSingleParameter(method.getMethod(), daoImplData, daoImplData.getEntity().getType())) {
                throw new IllegalStateException("Replace method should have only single entity parameter: " + method.getMethod());
            }
            MethodSpec.Builder builder = generateUtil.methodBuilder(method.getMethod(), daoImplData.getInterfaceType());
            CodeBlock.Builder operation = CodeBlock.builder();
            if (!isVoid(method)) {
                operation.add("final var result = ($T) ", LIST_LIST_OBJECT_TYPE);
            }
            operation.add("$L.syncOps().replace($L, $N($L))", CLIENT, SPACE, daoImplData.getToTuple(), method.getMethod().getParameters().get(0).getSimpleName());
            builder.addStatement(operation.build());
            if (!isVoid(method)) {
                addReturnEntity(daoImplData, builder);
            }
            return builder.build();
        }
    },
    UPDATE(List.of("update"), Update.class) {
        @Nonnull
        @Override
        public MethodSpec createMethodSpec(@Nonnull DaoMethodData method, @Nonnull DaoImplData daoImplData, @Nonnull GenerateUtil generateUtil) {
            MethodSpec.Builder builder = generateUtil.methodBuilder(method.getMethod(), daoImplData.getInterfaceType());
            CodeBlock.Builder operation = CodeBlock.builder();
            if (!isVoid(method)) {
                operation.add("final var result = ($T) ", LIST_LIST_OBJECT_TYPE);
            }
            List<? extends VariableElement> keyFields = method.getMethod().getParameters().stream()
                    .filter(not(isAnnotatedWith(OPERATIONS)))
                    .collect(Collectors.toList());
            if (keyFields.isEmpty()) {
                throw new IllegalStateException("Update method should have at least 1 key parameter: " + method.getMethod());
            }
            operation.add("$L.syncOps().update($L, $T.asList(", CLIENT, SPACE, Arrays.class)
                    .add(getArgsAsString(method, keyFields, daoImplData, generateUtil))
                    .add(")");
            addOperations(method, daoImplData, operation, generateUtil);
            builder.addStatement(operation.add(")").build());
            if (!isVoid(method)) {
                addReturnOptional(daoImplData, builder);
            }
            return builder.build();
        }
    },
    UPSERT(List.of("upsert"), Upsert.class) {
        @Nonnull
        @Override
        public MethodSpec createMethodSpec(@Nonnull DaoMethodData method, @Nonnull DaoImplData daoImplData, @Nonnull GenerateUtil generateUtil) {
            MethodSpec.Builder builder = generateUtil.methodBuilder(method.getMethod(), daoImplData.getInterfaceType());
            CodeBlock.Builder operation = CodeBlock.builder();
            List<? extends VariableElement> tupleFields = method.getMethod().getParameters().stream()
                    .filter(not(isAnnotatedWith(OPERATIONS))
                            .and(hasEntityType(daoImplData)))
                    .collect(Collectors.toList());
            if (tupleFields.size() != 1) {
                throw new IllegalStateException("Upsert method should have exact 1 entity parameter: " + method.getMethod());
            }
            String tupleName = tupleFields.get(0).getSimpleName().toString();
            operation.add("$L.syncOps().upsert($L, $N($L), $N($L)", CLIENT, SPACE, daoImplData.getToKey(), tupleName, daoImplData.getToTuple(), tupleName);
            addOperations(method, daoImplData, operation, generateUtil);
            builder.addStatement(operation.add(")").build());
            return builder.build();
        }
    },
    DELETE(List.of("delete"), Delete.class) {
        @Nonnull
        @Override
        public MethodSpec createMethodSpec(@Nonnull DaoMethodData method, @Nonnull DaoImplData daoImplData, @Nonnull GenerateUtil generateUtil) {
            MethodSpec.Builder builder = generateUtil.methodBuilder(method.getMethod(), daoImplData.getInterfaceType());
            CodeBlock.Builder operation = CodeBlock.builder();
            if (!isVoid(method)) {
                operation.add("final var result = ($T) ", LIST_LIST_OBJECT_TYPE);
            }
            operation.add("$L.syncOps().delete($L, ", CLIENT, SPACE);
            if (generateUtil.hasSingleParameter(method.getMethod(), daoImplData, daoImplData.getEntity().getType())) {
                operation.add("$N($L))", daoImplData.getToKey(), method.getMethod().getParameters().get(0).getSimpleName());
            } else {
                operation.add("$T.asList($L))", Arrays.class, getArgsAsString(method, method.getMethod().getParameters(), daoImplData, generateUtil));
            }
            builder.addStatement(operation.build());
            if (!isVoid(method)) {
                addReturnOptional(daoImplData, builder);
            }
            return builder.build();
        }
    },
    ;

    @Nonnull
    private static Predicate<Element> hasEntityType(@Nonnull final DaoImplData daoImplData) {
        return e -> e.asType().equals(daoImplData.getEntity().getType());
    }

    @Nonnull
    private static String lookupOperator(@Nonnull final VariableElement param) {
        if (isAnnotatedWith(param, Operation.Assign.class)) {
            return "=";
        } else if (isAnnotatedWith(param, Operation.Add.class)) {
            return "+";
        } else if (isAnnotatedWith(param, Operation.Sub.class)) {
            return "-";
        } else if (isAnnotatedWith(param, Operation.And.class)) {
            return "&";
        } else if (isAnnotatedWith(param, Operation.Or.class)) {
            return "|";
        } else if (isAnnotatedWith(param, Operation.Xor.class)) {
            return "^";
        } else {
            return "";
        }
    }

    @Nonnull
    @SuppressWarnings("unchecked")
    private static final Class<? extends Annotation>[] OPERATIONS = ((Class<? extends Annotation>[]) new Class[]{
            Operation.Assign.class, Operation.Add.class, Operation.Sub.class,
            Operation.And.class, Operation.Or.class, Operation.Xor.class});

    private static boolean isVoid(@Nonnull final DaoMethodData method) {
        return method.getMethod().getReturnType().getKind() == TypeKind.VOID;
    }

    private static void addReturnOptional(@Nonnull final DaoImplData daoImplData, @Nonnull final MethodSpec.Builder builder) {
        builder.beginControlFlow("if (result.isEmpty())")
                .addStatement("return $T.empty()", Optional.class)
                .endControlFlow()
                .addStatement("return $T.of($N(result.get(0)))", Optional.class, daoImplData.getToEntity());
    }

    private static void addReturnEntity(@Nonnull final DaoImplData daoImplData, @Nonnull final MethodSpec.Builder builder) {
        builder.addStatement("return $N(result.get(0))", daoImplData.getToEntity());
    }

    @Nonnull
    private static String getArgsAsString(
            @Nonnull final DaoMethodData method,
            @Nonnull final List<? extends VariableElement> parameters,
            @Nonnull final DaoImplData daoImplData,
            @Nonnull final GenerateUtil generateUtil
    ) {
        return parameters.stream()
                .sorted(Comparator.comparingInt(p -> getParamField(method, p, daoImplData).getOrder()))
                .map(p -> TupleUtil.findWriter(p.asType(), generateUtil)
                        .createGetter(p.asType(), p.getSimpleName().toString(), generateUtil))
                .collect(joining(", "));
    }

    @Nonnull
    private static FieldData getParamField(
            @Nonnull final DaoMethodData method,
            @Nonnull final VariableElement param,
            @Nonnull final DaoImplData daoImplData) {
        return daoImplData.getEntity().getFields().stream()
                .filter(f -> param.getSimpleName().contentEquals(f.getName()))
                .findAny()
                .orElseThrow(() -> new IllegalStateException("Can't map parameter '" + param.getSimpleName() + "' to any entity field : " + method.getMethod()));
    }

    private static void addOperations(
            @Nonnull final DaoMethodData method,
            @Nonnull final DaoImplData daoImplData,
            @Nonnull final CodeBlock.Builder builder,
            @Nonnull final GenerateUtil generateUtil
    ) {
        var operationParams = method.getMethod().getParameters().stream()
                .filter(isAnnotatedWith(OPERATIONS))
                .collect(Collectors.toList());
        if (operationParams.isEmpty()) {
            throw new IllegalStateException("Update/Upsert method should have at least 1 Operation parameter: " + method.getMethod());
        }
        operationParams.forEach(p -> {
            FieldData field = getParamField(method, p, daoImplData);
            builder.add(", $T.asList($S, $L, $L)", Arrays.class, lookupOperator(p), field.getOrder(),
                    TupleUtil.findWriter(p.asType(), generateUtil)
                            .createGetter(p.asType(), p.getSimpleName().toString(), generateUtil));
        });
    }

    private final List<String> prefixes;
    private final Class<? extends Annotation> annotationClass;

    public boolean supportsByAnnotation(@Nonnull final ExecutableElement method) {
        return AnnotationUtil.getAnnotationMirror(method, annotationClass).isPresent();
    }

    public boolean supportsByName(@Nonnull final ExecutableElement method) {
        return hasPrefix(method);
    }

    private boolean hasPrefix(@Nonnull final ExecutableElement method) {
        String methodName = method.getSimpleName().toString();
        return prefixes.stream().anyMatch(methodName::startsWith);
    }

    @Nonnull
    public abstract MethodSpec createMethodSpec(
            @Nonnull final DaoMethodData method,
            @Nonnull final DaoImplData daoImplData,
            @Nonnull final GenerateUtil generateUtil
    );
}
