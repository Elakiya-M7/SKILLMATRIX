package com.example.skillmatrix;

import org.springframework.boot.jackson.autoconfigure.JsonMapperBuilderCustomizer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import tools.jackson.databind.cfg.CoercionAction;
import tools.jackson.databind.cfg.CoercionInputShape;
import tools.jackson.databind.type.LogicalType;

@SpringBootApplication
public class SkillmatrixApplication {

	@Bean
	JsonMapperBuilderCustomizer strictStringInputCustomizer() {
		return builder -> {
			builder.withCoercionConfig(String.class, coercion -> {
				coercion.setCoercion(CoercionInputShape.Integer, CoercionAction.Fail);
				coercion.setCoercion(CoercionInputShape.Float, CoercionAction.Fail);
				coercion.setCoercion(CoercionInputShape.Boolean, CoercionAction.Fail);
				coercion.setCoercion(CoercionInputShape.Object, CoercionAction.Fail);
				coercion.setCoercion(CoercionInputShape.Array, CoercionAction.Fail);
				coercion.setCoercion(CoercionInputShape.EmptyObject, CoercionAction.Fail);
				coercion.setCoercion(CoercionInputShape.EmptyArray, CoercionAction.Fail);
			});
			builder.withCoercionConfig(LogicalType.Integer, coercion -> {
				coercion.setCoercion(CoercionInputShape.String, CoercionAction.Fail);
				coercion.setCoercion(CoercionInputShape.Float, CoercionAction.Fail);
				coercion.setCoercion(CoercionInputShape.Boolean, CoercionAction.Fail);
				coercion.setCoercion(CoercionInputShape.Object, CoercionAction.Fail);
				coercion.setCoercion(CoercionInputShape.Array, CoercionAction.Fail);
				coercion.setCoercion(CoercionInputShape.EmptyString, CoercionAction.Fail);
				coercion.setCoercion(CoercionInputShape.EmptyObject, CoercionAction.Fail);
				coercion.setCoercion(CoercionInputShape.EmptyArray, CoercionAction.Fail);
			});
			builder.withCoercionConfig(LogicalType.Enum, coercion -> {
				coercion.setCoercion(CoercionInputShape.Integer, CoercionAction.Fail);
				coercion.setCoercion(CoercionInputShape.Float, CoercionAction.Fail);
				coercion.setCoercion(CoercionInputShape.Boolean, CoercionAction.Fail);
				coercion.setCoercion(CoercionInputShape.Object, CoercionAction.Fail);
				coercion.setCoercion(CoercionInputShape.Array, CoercionAction.Fail);
				coercion.setCoercion(CoercionInputShape.EmptyObject, CoercionAction.Fail);
				coercion.setCoercion(CoercionInputShape.EmptyArray, CoercionAction.Fail);
			});
		};
	}

	public static void main(String[] args) {
		SpringApplication.run(SkillmatrixApplication.class, args);
	}

}
