package it.kytech.bowwarfare.api;

import java.util.HashMap;

/**
 * Created by M2K on 12/04/2015.
 */
public interface IScore {

    int getScore(String key);

    HashMap getScoreData();
}
