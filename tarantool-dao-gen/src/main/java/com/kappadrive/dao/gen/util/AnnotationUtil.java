package com.kappadrive.dao.gen.util;

import com.squareup.javapoet.AnnotationSpec;

import javax.annotation.Nonnull;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.AnnotationValueVisitor;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.SimpleAnnotationValueVisitor9;
import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class AnnotationUtil {

    private AnnotationUtil() {
    }

    @Nonnull
    @SafeVarargs
    public static List<AnnotationSpec> getAnnotationSpecs(@Nonnull final Element element, @Nonnull final Class<? extends Annotation>... exclude) {
        return element.getAnnotationMirrors().stream()
                .filter(a -> Stream.of(exclude).noneMatch(e -> a.getAnnotationType().toString().equals(e.getCanonicalName())))
                .map(AnnotationSpec::get)
                .collect(Collectors.toList());
    }

    @Nonnull
    public static Optional<AnnotationMirror> getAnnotationMirror(
            @Nonnull final Element element,
            @Nonnull final Class<? extends Annotation> annotationClass
    ) {
        return element.getAnnotationMirrors()
                .stream()
                .filter(a -> a.getAnnotationType().toString()
                        .equals(annotationClass.getCanonicalName()))
                .map(a -> (AnnotationMirror) a)
                .findAny();
    }

    @Nonnull
    public static Optional<AnnotationValue> getAnnotationValue(
            @Nonnull final AnnotationMirror annotationMirror,
            @Nonnull final String key
    ) {
        return annotationMirror.getElementValues().entrySet().stream()
                .filter(e -> e.getKey().getSimpleName().toString().equals(key))
                .map(e -> (AnnotationValue) e.getValue())
                .findAny();
    }

    @Nonnull
    public static <T> Optional<T> getAnnotationValue(
            @Nonnull final AnnotationMirror annotationMirror,
            @Nonnull final String key,
            @Nonnull final Class<? extends T> type
    ) {
        return getAnnotationValue(annotationMirror, key)
                .map(AnnotationValue::getValue)
                .map(type::cast);
    }

    @Nonnull
    public static <T> Optional<T> getAnnotationValue(
            @Nonnull final Element element,
            @Nonnull final Class<? extends Annotation> annotationClass,
            @Nonnull final String key,
            @Nonnull final Class<? extends T> type
    ) {
        return getAnnotationMirror(element, annotationClass)
                .flatMap(a -> getAnnotationValue(a, key, type));
    }

    @Nonnull
    public static <T extends Enum<T>> Optional<T> getAnnotationEnumValue(
            @Nonnull final Element element,
            @Nonnull final Class<? extends Annotation> annotationClass,
            @Nonnull final String key,
            @Nonnull final Class<T> enumType
    ) {
        return getAnnotationValue(element, annotationClass, key, Object.class)
                .map(Object::toString)
                .map(o -> Enum.valueOf(enumType, o));
    }

    @Nonnull
    public static <T> Optional<List<T>> getAnnotationArrayValue(
            @Nonnull final Element element,
            @Nonnull final Class<? extends Annotation> annotationClass,
            @Nonnull final String key,
            @Nonnull final AnnotationValueVisitor<? extends T, Object> visitor
    ) {
        return getAnnotationMirror(element, annotationClass)
                .flatMap(m -> getAnnotationValue(m, key))
                // do not optimize generic - jdk11 has unfixed bug
                .map(v -> v.accept(new SimpleAnnotationValueVisitor9<List<T>, Object>() {
                    @Override
                    public List<T> visitArray(List<? extends AnnotationValue> vals, Object o) {
                        return vals.stream().map(a -> a.accept(visitor, null)).collect(Collectors.toList());
                    }
                }, null));
    }

    @Nonnull
    public static <T> Optional<T> getAnnotationValue(
            @Nonnull final Element element,
            @Nonnull final Class<? extends Annotation> annotationClass,
            @Nonnull final Class<? extends T> type
    ) {
        return getAnnotationValue(element, annotationClass, "value", type);
    }

    @Nonnull
    public static AnnotationValueVisitor<TypeElement, Object> typeVisitor() {
        return new SimpleAnnotationValueVisitor9<TypeElement, Object>() {
            // do not optimize generic - jdk11 has unfixed bug
            @Override
            public TypeElement visitType(TypeMirror t, Object o) {
                return (TypeElement) ((DeclaredType) t).asElement();
            }
        };
    }
}
