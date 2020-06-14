package Models;

import Utilities.Constants;

public class Disease {
    private int mortalityRate;
    private int averageIllnessDuration;
    private int averageInfectionProbability;

    public Disease(int mortalityRate, int averageInfectionProbability, int averageIllnessDuration)
    {
        setMortalityRate(mortalityRate);
        this.averageInfectionProbability = averageInfectionProbability;
        this.averageIllnessDuration = averageIllnessDuration;
    }

    private void setMortalityRate(int mortalityRate)
    {
        if (this.mortalityRate < Constants.MIN_MORTALITY_RATE)
        {
            this.mortalityRate = Constants.MIN_MORTALITY_RATE;
        }
        else if (this.mortalityRate > Constants.MAX_MORTALITY_RATE)
        {
            this.mortalityRate = Constants.MAX_MORTALITY_RATE;
        }
        else
        {
            this.mortalityRate = mortalityRate;
        }

    }

    public int getMortalityRate() {
        return mortalityRate;
    }

    public int getAverageIllnessDuration() {
        return averageIllnessDuration;
    }

    public int getAverageInfectionProbability() {
        return averageInfectionProbability;
    }
}