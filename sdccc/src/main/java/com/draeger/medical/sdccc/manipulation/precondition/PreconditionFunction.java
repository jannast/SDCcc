/*
 * This Source Code Form is subject to the terms of the "SDCcc non-commercial use license".
 *
 * Copyright (C) 2025 Draegerwerk AG & Co. KGaA
 */

package com.draeger.medical.sdccc.manipulation.precondition;

/**
 * Functional interface for precondition verification calls.
 *
 * @param <One> argument type
 */
@FunctionalInterface
public interface PreconditionFunction<One> {

    /**
     * Applies the function with the given argument.
     *
     * @param one used in function
     * @return true if precondition is fulfilled, false otherwise
     * @throws PreconditionException if manipulation fails
     */
    boolean apply(One one) throws PreconditionException;
}
