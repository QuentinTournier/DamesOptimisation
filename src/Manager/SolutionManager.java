package Manager;

import object.Solution;

import java.util.*;

/**
 * Created by Quentin on 15/03/2017.
 */
public class SolutionManager {

    public List<Solution> getVoisins(Solution solution){

        ArrayList<Solution> listSolutions = new ArrayList<Solution>();
        for(int i=0;i<solution.getSize()-1;i++){
            for(int j=i+1;j<solution.getSize();j++){
                listSolutions.add(new Solution(solution.change(solution.getState(),i,j)));
            }
        }
        return listSolutions;
    }

    public double randomSolutionAverage(int n){
        double somme=0;
        double nbSolutions = 10;
        for (int i=0; i<nbSolutions; i++){
            Solution solution = new Solution(n);
            somme+=solution.getNbConflicts();
        }
        return somme/nbSolutions;
    }

    public Solution getOtherSolution(Solution solution){
        Random r = new Random();
        int random =r.nextInt(solution.getSize());
        int random2= random;
        while (random2 == random){
            random2 = r.nextInt(solution.getSize());
        }
        Solution otherSolution = new Solution(solution.getState());
        otherSolution.setState(otherSolution.change(otherSolution.getState(),random,random2));
        return otherSolution;
    }

    public Solution recuitSimulte(int n, double mu){
        //intitialize
        Solution xi = new Solution(n);
        Solution xmin = xi;
        int fmin = xmin.getNbConflicts();
        double proba = 0.3;
        double average = randomSolutionAverage(n);
        double t = -average/Math.log(proba);
        //double mu = 0.1;
        double n1 = n*2;//(Math.log(-average/t*Math.log(proba)))/Math.log(mu);

        for (int indBoucle1 = 0; indBoucle1 < Math.ceil(n1); indBoucle1++){
            for(int i=0;i<Math.ceil(n1);i++){
                Solution otherSolution = this.getOtherSolution(xi); // On prend une solution au hasard
                int delta = otherSolution.getNbConflicts() - xi.getNbConflicts();
                if(delta <= 0 ){
                    xi = new Solution(otherSolution.getState());
                    int fx = otherSolution.getNbConflicts();
                    if(fx < fmin){
                        fmin = fx;
                        xmin = new Solution(otherSolution.getState());
                        if(fmin == 0){
                            return xmin;
                        }
                    }
                }
                else{
                    Random random = new Random();
                    double p = random.nextDouble();
                    if( p<= Math.exp(-delta/t)){
                        xi = new Solution(otherSolution.getState());
                    }
                }
            }
            t *= mu;
        }
        System.out.print(fmin);
        return xmin;
    }

    public Solution tabou(int n, int allowed){
        Solution xi = new Solution(n);
        Solution xmin = new Solution(xi.getState());
        int fmin = xmin.getNbConflicts();
        int fi = fmin;
        Solution xVoisin;
        int fVoisin;
        List<Solution> voisins = new ArrayList<Solution>();
        List<Integer> forbiddenMoves = new ArrayList<Integer>();
        int forbiddenMoveAllowedSize= allowed;
        int i= 0;
        int nbIterations = 200;
        do{
            voisins = this.getTabouVoisins(xi, forbiddenMoves);
            if(!voisins.isEmpty()){
                xVoisin = this.getBestSolution(voisins);
                fVoisin = xVoisin.getNbConflicts();
                if(fVoisin >= fi){
                    if(forbiddenMoves.size() >= 2*forbiddenMoveAllowedSize){
                        forbiddenMoves.remove(1);
                        forbiddenMoves.remove(0);
                    }
                    forbiddenMoves.addAll(this.getPreviousMove(xVoisin, xi));
                }
                if(fVoisin < fmin){
                    xmin = new Solution(xVoisin.getState());
                    fmin = fVoisin;
                }
                xi = new Solution(xVoisin.getState());
                fi = xi.getNbConflicts();
            }
            i++;
        }while (!voisins.isEmpty() && i<nbIterations && fmin!=0);
        return xmin;
    }

    private List<Solution> getTabouVoisins(Solution solution, List<Integer> forbiddenMoves) {
        Solution copySolution = new Solution(solution.getState());
        ArrayList<Solution> listSolutions = new ArrayList<Solution>();
        for(int i=0;i<copySolution.getSize()-1;i++){
            loopsolution: for(int j=i+1;j<copySolution.getSize();j++){
                copySolution = new Solution(solution.getState());
                for(int forbidden = 0; forbidden<forbiddenMoves.size();forbidden++){
                    if (forbiddenMoves.get(forbidden) == i){
                        if(forbidden%2 == 0){
                            if(forbiddenMoves.get(forbidden + 1) == j){
                                continue loopsolution;
                            }
                        }else{
                            if(forbiddenMoves.get(forbidden - 1) == j){
                                continue loopsolution;
                            }
                        }
                    }
                }
                listSolutions.add(new Solution(copySolution.change(copySolution.getState(),i,j)));
            }
        }
        return listSolutions;
    }


    private ArrayList<Integer> getPreviousMove(Solution xVoisin, Solution xi) {
        ArrayList<Integer> move = new ArrayList<>();
        for(int i=0; i<xVoisin.getSize();i++){
            if(xVoisin.getIState(i) != xi.getIState(i)){
                move.add(i);
            }
        }
        return move;
    }

    private Solution getBestSolution(List<Solution> voisins) {
        Solution sBest = null;
        int fBest = Integer.MAX_VALUE;
        int i=0;
        int f;
        for (Solution solution : voisins){
            f= solution.getNbConflicts();
            if(f<fBest){
                fBest = f;
                sBest = new Solution(solution.getState());
            }
            i++;
        }
        return new Solution(sBest.getState());
    }

}
