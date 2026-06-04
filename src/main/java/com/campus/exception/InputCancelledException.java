package com.campus.exception;

/**
 * Signals that the user abandoned the current input prompt — either by typing
 * the back keyword ('b') or by entering a value that failed validation.
 *
 * It is unchecked so the input helpers can throw it without every menu method
 * declaring it; the enclosing menu loop catches it and simply redisplays the
 * menu, instead of re-prompting the same value back-to-back.
 */
public class InputCancelledException extends RuntimeException {
    public InputCancelledException(String message) {
        super(message);
    }
}
