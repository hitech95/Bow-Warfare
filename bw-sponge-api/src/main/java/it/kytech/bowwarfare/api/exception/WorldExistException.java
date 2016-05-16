/**
 * This file is part of BowWarfare
 *
 * Copyright (c) 2016 hitech95 <https://github.com/hitech95>
 * Copyright (c) contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package it.kytech.bowwarfare.api.exception;

import org.spongepowered.api.text.Text;

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
        return message == null ? null : message.toPlain();
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
