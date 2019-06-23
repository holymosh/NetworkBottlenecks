package helpers;
import org.jgrapht.graph.DefaultDirectedWeightedGraph;
import org.jgrapht.graph.DefaultUndirectedWeightedGraph;
import org.jgrapht.graph.DefaultWeightedEdge;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class DataSetWriter {
    LocalDateTime localDateTime;
    String path;
    Map<DefaultWeightedEdge,Double> initialEdges;
    public DataSetWriter(Map<DefaultWeightedEdge,Double> initialEdges){
        path = "C:\\study\\diploma\\"+ LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME).replace(':','-').replace('.','-');
        File file = new File(path);
        System.out.println(file.getPath());
        file.mkdir();
        try {
            file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.initialEdges = initialEdges;
    }

    public void WriteDataSet(DefaultUndirectedWeightedGraph<Integer,DefaultWeightedEdge> graph, int iteration){
        List<Integer> vertexes = graph.vertexSet().stream().filter(i -> graph.degreeOf(i) > 3 ).collect(Collectors.toList());
        PrintCapacity(graph,iteration);
        PrintZeroEdges(graph,iteration);
        System.out.println("Vertex with degree more than 3- " + vertexes.size());
        ArrayList<Pair<Integer>> pairs = new ArrayList<>();
        for (int i = 0; i < vertexes.size() - 1; i++) {
            for (int j = i+1; j < vertexes.size() ; j++) {
                pairs.add(new Pair<>(vertexes.get(i),vertexes.get(j)));
            }
        }
        ArrayList<Double> minCutValuesBefore = new ArrayList<>();
        MinCutHelper<Integer, DefaultWeightedEdge> cutHelper = new MinCutHelper<>(graph);
        Map<DefaultWeightedEdge,Integer> edgeDictionary = new HashMap<>();
        for (int i = 0; i < pairs.size(); i++) {
            Pair<Integer> pair = pairs.get(i);
            List<Set<DefaultWeightedEdge>> minCutsBetweenVertexes =
                    cutHelper.getAllMinCuts2(pair.getV1(),pair.getV2());
            List<DefaultWeightedEdge> edges = minCutsBetweenVertexes.stream().flatMap(dwes -> dwes.stream()).collect(Collectors.toList());
            minCutValuesBefore.add(cutHelper.getMaxFlow());
            for (DefaultWeightedEdge edge : edges) {
                if (!edgeDictionary.containsKey(edge)){
                    edgeDictionary.put(edge,1);
                }
                else {
                    Integer oldValue = edgeDictionary.get(edge);
                    edgeDictionary.replace(edge,oldValue,oldValue+1);
                }
            }
        }
        PrintMinCutsValues(minCutValuesBefore,iteration);
        PrintPercent(graph,iteration);
    }

    private void PrintZeroEdges(DefaultUndirectedWeightedGraph<Integer, DefaultWeightedEdge> graph, int iteration) {
        try {
            String localPath = path+"/zeroEdges/";
            new File(localPath).mkdir();
            PrintWriter printWriter = new PrintWriter( localPath+ iteration +"zeroEdges.csv");
            System.out.println(graph.edgeSet().stream().filter(dwe-> graph.getEdgeWeight(dwe) == 0.0).count());
            printWriter.println();
            printWriter.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void PrintMinCutsValues(ArrayList<Double> values,int i){
        try {
            String localPath = path+"/mincuts/";
            new File(localPath).mkdir();
            PrintWriter printWriter = new PrintWriter( localPath+ i +"MinCuts.csv");
            values.forEach(aDouble -> printWriter.println(aDouble+","));
            printWriter.println();
            printWriter.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
    private void PrintCapacity(DefaultUndirectedWeightedGraph<Integer, DefaultWeightedEdge> graph, int i){
        try {
            String localPath = path+"/capacities/";
            new File(localPath).mkdir();
            PrintWriter printWriter = new PrintWriter(localPath+ i +"capacities.csv");
            graph.edgeSet().forEach(dwE -> printWriter.println(graph.getEdgeWeight(dwE) +","));
            printWriter.println();
            printWriter.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void PrintPercent(DefaultUndirectedWeightedGraph<Integer, DefaultWeightedEdge> graph, int i){
        try {
            String localPath = path+"/percents/";
            new File(localPath).mkdir();
            PrintWriter printWriter = new PrintWriter(localPath+ i +"percents.csv");
            graph.edgeSet().forEach(dwE
                    -> printWriter.println((graph.getEdgeWeight(dwE)/initialEdges.get(dwE)) +","));
            printWriter.println();
            printWriter.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
    }
