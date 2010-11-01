package usr.common;


import usr.logging.*;

public class ProbElement
{

    int distType_= 0;
    static final int EXPO_DIST= 1;  // Exponential
    static final int GAMMA_DIST= 2;
    static final int WEIBULL_DIST= 3;
    static final int UNIFORM_DIST= 4;
    static final int PARETO_DIST= 5;
    static final int POISSON_DIST= 6;  // Poisson Distribution
    static final int POISSON_MIN_DIST= 7; // Poisson with min value parm  
    static final int POISSON_PLUS_DIST= 8;  // Poisson with second value parm added
    static final int LOG_NORMAL_DIST= 9;  // Lognormal variate
    static final int NORMAL_DIST= 10;  // Normal variate
    double weight_;
    double parm1_= 0.0;
    double parm2_= 0.0;
    double parm3_= 0.0;
    
    
    public ProbElement(String typeStr, double []parms) throws ProbException
    {
    
        this(typeStr, 1.0, parms);
    }
    public ProbElement(String typeStr, double weight, double []parms) 
        throws ProbException {
       
        int type= 0;
        if (typeStr.equals("Exponential")) {
           type= EXPO_DIST;
        } else if (typeStr.equals("Gamma")) {
           type= GAMMA_DIST;
        } else if (typeStr.equals("Weibull")) {
           type= WEIBULL_DIST;
        } else if (typeStr.equals("Uniform")) {
           type= UNIFORM_DIST;
        } else if (typeStr.equals("Pareto")) {
           type= PARETO_DIST;
        } else if (typeStr.equals("Poisson")) {
           type= POISSON_DIST;
        } else if (typeStr.equals("PoissonMin")) {
           type= POISSON_MIN_DIST;
        } else if (typeStr.equals("PoissonPlus")) {
           type= POISSON_PLUS_DIST;
        } else if (typeStr.equals("LogNormal")) {
           type= LOG_NORMAL_DIST;
        } else if (typeStr.equals("Normal")) {
           type= NORMAL_DIST;
        }
        
          
        if (type == 0) {
           throw new ProbException("Unknown distribution type");
        }
        distType_= type;
        weight_= weight;
        //Logger.getLogger("log").logln(USR.STDOUT, "Weight "+weight+" type "+type);
        if (parms.length > 0) {
           parm1_= parms[0];
        }
        if (parms.length > 1) {
           parm2_= parms[1];
        }
        if (parms.length > 2) {
           parm3_= parms[2];
        }
           
    }
    
    /**Return the *cumulative* distribution function value at x*/
    public double getCumulativeDistribution(double x) throws ProbException {
         if (distType_ == EXPO_DIST) {
            return exponentialDist(x,parm1_);
        }
        if (distType_ == WEIBULL_DIST) {
            return weibullDist(x,parm1_,parm2_);
        }
        if (distType_ == GAMMA_DIST) {
            return gammaDist(x,parm1_, parm2_);
        }
        if (distType_ == UNIFORM_DIST) {
            return uniformDist(x,parm1_, parm2_);
        }
        if (distType_ == PARETO_DIST) {
            return paretoDist(x,parm1_, parm2_);
        }
        if (distType_ == POISSON_DIST) {
            return poissonDist(x,parm1_);
        }
        if (distType_ == POISSON_MIN_DIST) {
            return poissonMinDist(x,parm1_,parm2_);
        }
        if (distType_ == POISSON_PLUS_DIST) {
            return poissonDist(x-parm2_,parm1_);
        }
        if (distType_ == LOG_NORMAL_DIST) {
            return logNormalDist(x,parm1_,parm2_);
        }
        if (distType_ == NORMAL_DIST) {
            return normalDist(x,parm1_,parm2_);
        }
        throw new ProbException("Unknown distribution type");
        
    }

    /** Cum Dist function for exponential*/
    double exponentialDist(double x, double mean) {
        return 1.0 - Math.exp(-x/mean);
    }

    /** Cum dist function for weibull */
    double weibullDist(double x, double lambda, double beta) 
    {
        return (1.0- Math.exp(-Math.pow(x/lambda,beta)));
    }
    
    /** Cum dist function for gamma Function */
    double gammaDist(double y, double shape, double scale) throws ProbException
    {
        throw new ProbException("Not yet written gammaDist");
    }
    
    /** Cum dist function for unifrom Function */
    double uniformDist(double x, double min, double max) 
    {
        return ((x-min)/(max-min));
    }
    
     /** Cum dist function for Pareto dist */
    double paretoDist(double x, double scale, double shape) 
    {
        return (1.0 -Math.pow(scale/x,shape));
    }
    
    /** Cum dist function for Poisson */
    double poissonDist(double x, double mean)
    {
        double dist= 0.0;
        double fact= 1.0;
        for (int i= 0; i <= x; i++) {
            if (i > 1) {
                fact*= i;
            }
            dist+= Math.exp(-mean)*Math.pow(mean,i)/fact;
            
        }     
        return dist;
    }
    
    /** Cum dist function for Poisson */
    double poissonMinDist(double x, double mean, double min)
    {
        if (x < min) {
            return 0;
        }
        double dist= 0.0;
        double fact= 1.0;
        for (int i= 0; i <= x; i++) {
            if (i > 1) {
                fact*= i;
            }
            dist+= Math.exp(-mean)*Math.pow(mean,i)/fact;
            
        }     
        return dist;
    }
    
    /** Cum dist function for log Normal */
    double logNormalDist(double x, double mu, double sd)
    {   
        return 0.5+0.5*MathFunctions.erf((Math.log(x)-mu)/(sd*Math.sqrt(2.0)));
    }

    /** Cum dist for normal Dist */
    double normalDist(double x, double mu, double sd)
    { 
        return 0.5+ 0.5*MathFunctions.erf((x-mu)/(sd*Math.sqrt(2.0)));
    }
    
    public double getVariate() throws ProbException {
        if (distType_ == EXPO_DIST) {
            return exponentialVariate(parm1_);
        }
        if (distType_ == WEIBULL_DIST) {
            return weibullVariate(parm1_,parm2_);
        }
        if (distType_ == GAMMA_DIST) {
            return gammaVariate(parm1_, parm2_);
        }
        if (distType_ == UNIFORM_DIST) {
            return uniformVariate(parm1_, parm2_);
        }
        if (distType_ == PARETO_DIST) {
            return paretoVariate(parm1_, parm2_);
        }
        if (distType_ == POISSON_DIST) {
            return poissonVariate(parm1_);
        }
        if (distType_ == POISSON_MIN_DIST) {
            return Math.max(parm1_,poissonVariate(parm1_));
        }
        if (distType_ == POISSON_PLUS_DIST) {
            double ans= parm2_+poissonVariate(parm1_);
            return ans;
        }
        if (distType_ == LOG_NORMAL_DIST) {
            return logNormalVariate(parm1_,parm2_);
        }
        if (distType_ == NORMAL_DIST) {
            return normalVariate(parm1_,parm2_);
        }
        throw new ProbException("Unknown distribution type");
        
    }

    public double getWeight() {
        return weight_;
    }

    public static double weibullVariate(double lambda, double beta) {
        //Weibulls wobble but they don't fall down
        double power,x;
        double p= Math.random();
        power = 1.0/beta;
        x = lambda*Math.pow(-Math.log(1.0 - p),power);

        return x;

   }
   public static double exponentialVariate(double mean) {
        // uses mean not rate
        double p, x;
        p= Math.random();
        x = - Math.log(1.0 - p)*mean;

        return x;

   }
   
   /** Generate Gamma variate taken from: JSTOR vol 23 no 3 pp 290-295 --
   there are faster methods but this is simple though it does look mental*/

    public static double gammaVariate (double shape, double scale)
    {
          double a= shape -1.0;
          double b= (shape- 1.0/(6.0*shape))/a;
          double c= 2.0/a;
          double d= c+ 2.0;
          
          double U1, U2,U,W;
          
          while(true) {
             U1= Math.random();
             
             if (scale <= 2.5) {
                 U2= Math.random();
             } else {
                 while(true) {
                    U= Math.random();
                    U2= U1 + (1.0 - 1.86*U)/Math.sqrt(shape);
                    if (U2 < 1.0 && U2 > 0.0) 
                        break;
                 }
             }
             W= b*U1/U2;
             if (c*U2-d+W+1.0/W <= 0.0) 
                 break;
             if (c*Math.log(U2) - Math.log(W)+W-1.0 < 0.0)
                 break;
          }
          return scale*a*W;
    }
   
    
    public static double uniformVariate(double min, double max) 
    {
        double x;
        x= Math.random();
        x= x*(max-min)+min;
        return x;
    }
    
    public static double paretoVariate(double scale, double shape)
    {
        double x, U;
        U= exponentialVariate(1.0/shape);
        x= scale*Math.exp(U);
        return x;
    }
    
    public static double poissonVariate(double mean)
    {   
       
        double p= 1.0;
        double L= Math.exp(-mean);
        int k= 0;
        do {
            k= k+1;
            p=p*Math.random();
        } while (p > L);
        return (double)(k-1);

    }
    
    /** Generate a log Normal variate */
    public static double logNormalVariate(double mu, double sd)
    {
        double normal = normalVariate(0,1.0);
        double lognormal =  Math.exp(mu+sd*normal);
        //System.out.println("Normal "+normal + " lognormal "+lognormal);
        return lognormal;
    }
    
    /** Standard java to generate normal Variate -- actually
    generates two one of which is binned */
    
    public static double normalVariate(double mean, double sd)
    {
            double v1, v2, s;
            do { 
                    v1 = 2 * Math.random() - 1;   // between -1.0 and 1.0
                    v2 = 2 * Math.random() - 1;   // between -1.0 and 1.0
                    s = v1 * v1 + v2 * v2;
            } while (s >= 1 || s == 0);
            double multiplier = Math.sqrt(-2 * Math.log(s)/s);
          
            double g= v1 * multiplier;

        return g*sd + mean;
    }
}
