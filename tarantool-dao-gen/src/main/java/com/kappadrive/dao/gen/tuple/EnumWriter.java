package com.kappadrive.dao.gen.tuple;

import com.kappadrive.dao.api.EnumMapper;
import com.kappadrive.dao.gen.util.AnnotationUtil;
import com.kappadrive.dao.gen.util.GenerateUtil;
import com.squareup.javapoet.CodeBlock;

import javax.annotation.Nonnull;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import static com.kappadrive.dao.gen.tuple.TupleUtil.simpleCast;

class EnumWriter implements TupleTypeWriter {
    @Override
    public boolean supportType(@Nonnull TypeMirror typeMirror, @Nonnull GenerateUtil generateUtil) {
        return generateUtil.isEnum(typeMirror);
    }

    @Nonnull
    @Override
    public String createGetter(@Nonnull TypeMirror type, @Nonnull String value, @Nonnull GenerateUtil generateUtil) {
        Optional<Element> mapper = findMapper(type);
        CharSequence enumGetter = mapper.flatMap(EnumWriter::findGetter).orElse("name");
        String result = String.format("%s == null ? null : %s.%s()", value, value, enumGetter);
        if (mapper.isPresent()) {
            TypeMirror mapperType = mapper.get().asType();
            return TupleUtil.findWriter(mapperType, generateUtil)
                    .createGetter(mapperType, result, generateUtil);
        }
        return result;
    }

    @Nonnull
    @Override
    public CodeBlock createOptionalSetter(@Nonnull TypeMirror type, @Nonnull GenerateUtil generateUtil) {
        Optional<Element> mapper = findMapper(type);
        if (mapper.isPresent()) {
            TypeMirror mapperType = mapper.get().asType();
            CharSequence enumGetter = findGetter(mapper.get()).orElse("name");
            return TupleUtil.findWriter(mapperType, generateUtil)
                    .createOptionalSetter(mapperType, generateUtil)
                    .toBuilder()
                    .add(".flatMap(e -> $T.of($T.values()).filter(v -> $T.equals(e, v.$L())).findAny())",
                            Stream.class, type, Objects.class, enumGetter)
                    .build();
        } else {
            return simpleCast(String.class).toBuilder().add(".map($T::valueOf)", type).build();
        }
    }

    @Nonnull
    private static Optional<Element> findMapper(@Nonnull TypeMirror type) {
        return ((DeclaredType) type).asElement().getEnclosedElements().stream()
                .filter(AnnotationUtil.isAnnotatedWith(EnumMapper.class))
                .map(e -> (Element) e)
                .findAny();
    }

    @Nonnull
    private static Optional<CharSequence> findGetter(@Nonnull final Element element) {
        if (element.getKind() == ElementKind.FIELD) {
            return Optional.of(GenerateUtil.createGetter((VariableElement) element));
        } else if (element.getKind() == ElementKind.METHOD) {
            return Optional.of(element.getSimpleName());
        }
        return Optional.empty();
    }
}
