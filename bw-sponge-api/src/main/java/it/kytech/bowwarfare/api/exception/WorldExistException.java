package it.kytech.bowwarfare.api.exception;

import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.Texts;

import javax.annotation.Nullable;

/**
 * Created by Hitech95 on 27/06/2015.
 */
public class WorldExistException extends Exception {
    private static final long serialVersionUID = 8933751632078553774L;

    @Nullable
    private final Text message;

    /**
     * Constructs a new {@link WorldExistException}.
     */
    public WorldExistException() {
        this.message = null;
    }

    /**
     * Constructs a new {@link WorldExistException} with the given message.
     *
     * @param message The detail message
     */
    public WorldExistException(Text message) {
        this.message = message;
    }

    /**
     * Constructs a new {@link WorldExistException} with the given message and cause.
     *
     * @param message   The detail message
     * @param throwable The cause
     */
    public WorldExistException(Text message, Throwable throwable) {
        super(throwable);
        this.message = message;
    }

    /**
     * Constructs a new {@link WorldExistException} with the given cause.
     *
     * @param throwable The cause
     */
    public WorldExistException(Throwable throwable) {
        super(throwable);
        this.message = null;
    }

    @Override
    @Nullable
    public String getMessage() {
        Text message = getText();
        return message == null ? null : Texts.toPlain(message);
    }

    /**
     * Returns the text message for this exception, or null if nothing is present.
     *
     * @return The text for this message
     */
    @Nullable
    public Text getText() {
        return this.message;
    }

    @Override
    @Nullable
    public String getLocalizedMessage() {
        return getMessage();
    }
}
