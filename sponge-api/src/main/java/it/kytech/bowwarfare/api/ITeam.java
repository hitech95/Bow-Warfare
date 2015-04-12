package it.kytech.bowwarfare.api;

import org.spongepowered.api.text.format.TextColor;

/**
 * This is a Team, you can access the Name and other parm of the team
 */
public interface ITeam {

    String getName();

    String getSlug();

    String getDescription();

    TextColor getColor();
}
