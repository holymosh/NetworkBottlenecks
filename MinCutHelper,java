package helpers;

import org.jgrapht.alg.flow.EdmondsKarpMFImpl;
import org.jgrapht.graph.AsUndirectedGraph;
import org.jgrapht.graph.DefaultDirectedWeightedGraph;
import org.jgrapht.graph.DefaultUndirectedWeightedGraph;

import java.util.*;

public class MinCutHelper<V,E> {
    private double minCutValue;
    private EdmondsKarpMFImpl<V, E> veEdmondsKarpMF;
    DefaultDirectedWeightedGraph<V,E> copied;
    List<Set<E>> minCuts;

    public MinCutHelper(DefaultUndirectedWeightedGraph<V, E> initialGraph) {
        minCuts = new ArrayList<>();
        copied = (DefaultDirectedWeightedGraph<V, E>) initialGraph.clone();
    }

    public List<Set<E>> getAllMinCuts2(V from, V to){
        minCuts = new ArrayList<>();
        veEdmondsKarpMF = new EdmondsKarpMFImpl<>(new AsUndirectedGraph(copied));
        minCutValue = veEdmondsKarpMF.calculateMinCut(from,to);
        double currentValue = minCutValue;
        Map<E,Double> initial = new HashMap<>();
        while (currentValue == minCutValue){
            Set<E> minCut = veEdmondsKarpMF.getCutEdges();
            minCut.forEach(e -> {
                if (!initial.containsKey(e)) initial.put(e,copied.getEdgeWeight(e));
            });
            minCuts.add(minCut);
            for (E e : minCut) {
                copied.setEdgeWeight(e,copied.getEdgeWeight(e)+1);
                veEdmondsKarpMF = new EdmondsKarpMFImpl<>(new AsUndirectedGraph(copied));
                double secondAttempt = veEdmondsKarpMF.calculateMinCut(from,to);
                if (secondAttempt == minCutValue) {
                    Set<E> newCut = veEdmondsKarpMF.getCutEdges();
                    newCut.forEach(e1 -> {
                        if (!initial.containsKey(e1)) initial.put(e1,copied.getEdgeWeight(e1));
                    });
                    minCuts.add(veEdmondsKarpMF.getCutEdges());
                }
                copied.setEdgeWeight(e,copied.getEdgeWeight(e) - 1);
            }
            minCut.stream().forEach(e -> copied.setEdgeWeight(e,copied.getEdgeWeight(e)+1));
            veEdmondsKarpMF = new EdmondsKarpMFImpl<>(new AsUndirectedGraph(copied));
            currentValue = veEdmondsKarpMF.calculateMinCut(from,to);
        }
        for (E e : initial.keySet()) {
            copied.setEdgeWeight(e,initial.get(e));
        }
        return minCuts;
    }

    public double getMaxFlow(){
        return minCutValue;
    }
}
