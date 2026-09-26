package br.com.eterniaserver.ffut.domain.challenge.enums;

public enum MutationType {

    CONDITIONAL_BOUNDARY,
    INCREMENTS,
    INVERT_NEGATIVES,
    MATH,
    NEGATE_CONDITIONALS,
    VOID_METHOD_CALLS,
    EMPTY_RETURNS,
    FALSE_RETURNS,
    TRUE_RETURNS,
    NULL_RETURNS,
    PRIMITIVE_RETURNS;

    public static MutationType getEnum(String mutationType) {
        return switch (mutationType) {
            case "org.pitest.mutationtest.engine.gregor.mutators.ConditionalsBoundaryMutator" -> CONDITIONAL_BOUNDARY;
            case "org.pitest.mutationtest.engine.gregor.mutators.IncrementsMutator" -> INCREMENTS;
            case "org.pitest.mutationtest.engine.gregor.mutators.InvertNegsMutator" -> INVERT_NEGATIVES;
            case "org.pitest.mutationtest.engine.gregor.mutators.MathMutator" -> MATH;
            case "org.pitest.mutationtest.engine.gregor.mutators.NegateConditionalsMutator" -> NEGATE_CONDITIONALS;
            case "org.pitest.mutationtest.engine.gregor.mutators.VoidMethodCallMutator" -> VOID_METHOD_CALLS;
            case "org.pitest.mutationtest.engine.gregor.mutators.returns.EmptyObjectReturnValsMutator" -> EMPTY_RETURNS;
            case "org.pitest.mutationtest.engine.gregor.mutators.returns.BooleanFalseReturnValsMutator" -> FALSE_RETURNS;
            case "org.pitest.mutationtest.engine.gregor.mutators.returns.BooleanTrueReturnValsMutator" -> TRUE_RETURNS;
            case "org.pitest.mutationtest.engine.gregor.mutators.returns.NullReturnValsMutator" -> NULL_RETURNS;
            case "org.pitest.mutationtest.engine.gregor.mutators.returns.PrimitiveReturnsMutator" -> PRIMITIVE_RETURNS;
            default -> null;
        };
    }
}
