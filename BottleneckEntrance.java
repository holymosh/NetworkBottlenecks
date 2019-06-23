import TopologyTypes.TopologyType;
import com.mxgraph.layout.mxCircleLayout;
import com.mxgraph.model.mxGraphModel;
import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.util.mxConstants;
import com.mxgraph.util.mxUtils;
import helpers.DataSetWriter;
import helpers.MinCutHelper;
import helpers.Pair;
import helpers.Three;
import org.jgrapht.alg.shortestpath.DijkstraShortestPath;
import org.jgrapht.ext.JGraphXAdapter;
import org.jgrapht.generate.GnmRandomGraphGenerator;
import org.jgrapht.graph.AsUndirectedGraph;
import org.jgrapht.graph.DefaultUndirectedWeightedGraph;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.util.SupplierUtil;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class BottleneckEntrance
        extends
        JApplet
{
    private static final long serialVersionUID = 2202072534703043194L;

    private static final Dimension DEFAULT_SIZE = new Dimension(530, 320);

    private JGraphXAdapter<Integer, DefaultWeightedEdge> jgxAdapter;

    public static void main(String[] args)
    {
        BottleneckEntrance applet = new BottleneckEntrance();
        applet.init();
        JFrame frame = new JFrame();
        frame.getContentPane().add(applet);
        frame.setTitle("Bottlenecks");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }

    @Override
    public void init()
    {
        int size = 100;
        Random random = new Random();

        Supplier<Integer> vSupplier = new Supplier<Integer>()
        {
            private Integer id = 0;

            @Override
            public Integer get()
            {
                return id++;
            }
        };
        DefaultUndirectedWeightedGraph<Integer,DefaultWeightedEdge> graph =
                new DefaultUndirectedWeightedGraph<>(vSupplier, SupplierUtil.createDefaultWeightedEdgeSupplier());
        int maxWeight = 60;
        GnmRandomGraphGenerator<Integer,DefaultWeightedEdge> generator = new GnmRandomGraphGenerator<>(40,130);
        generator.generateGraph(graph);
        graph.edgeSet().stream().forEach(edge -> {
            int weight = random.nextInt(maxWeight);
            weight = weight < 22 ? weight+17 : weight;
            graph.setEdgeWeight(edge, weight);
        });
        Map<DefaultWeightedEdge,Double> initialCaps = new HashMap<>();
        graph.edgeSet().forEach(defaultWeightedEdge -> initialCaps.put(defaultWeightedEdge,graph.getEdgeWeight(defaultWeightedEdge)));
        Map<DefaultWeightedEdge,Double> initial = new HashMap<>();
        graph.edgeSet().forEach(dwe -> initial.put(dwe,graph.getEdgeWeight(dwe)));
        graph.edgeSet().forEach(dwe -> System.out.print(graph.getEdgeWeight(dwe)+", "));
        List<Pair<Integer>> flows = putFlows(graph,40,initialCaps);
        List<Integer> vertexes = graph.vertexSet().stream().filter(i -> graph.degreeOf(i) > 3 ).collect(Collectors.toList());
        System.out.println("Vertex with degree more than 3- " + vertexes.size());
        ArrayList<Pair<Integer>> pairs = new ArrayList<>();
        for (int i = 0; i < vertexes.size() - 1; i++) {
            for (int j = i+1; j < vertexes.size() ; j++) {
                pairs.add(new Pair<>(vertexes.get(i),vertexes.get(j)));
            }
        }
        ArrayList<Double> minCutValuesBefore = new ArrayList<>();
        MinCutHelper<Integer,DefaultWeightedEdge> cutHelper = new MinCutHelper<>(graph);
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
        minCutValuesBefore.forEach(aDouble -> System.out.print(aDouble+","));
        System.out.println();

        System.out.println("bandwidth");
        graph.edgeSet().forEach(b -> System.out.print(graph.getEdgeWeight(b)+","));
        System.out.println("");
        System.out.println("величины минимальных разрезов "+ minCutValuesBefore.size());
        minCutValuesBefore.stream().mapToInt(value -> value.intValue()).forEach(aDouble -> System.out.print(aDouble +","));
        System.out.println("");
        System.out.println("вхождения одной дуги в минимал  ьный разрез "+ edgeDictionary.size()+"/"+ graph.edgeSet().size());
        edgeDictionary.values().forEach(integer -> System.out.print(integer + ","));
        edgeDictionary.keySet().forEach(defaultWeightedEdge -> System.out.println(defaultWeightedEdge.toString()+" " + graph.getEdgeWeight(defaultWeightedEdge)));
        List<Three<DefaultWeightedEdge>> threeSet = edgeDictionary.entrySet().stream().map(entry ->
                new Three<>(entry.getKey(),
                        graph.getEdgeWeight(entry.getKey()),
                        entry.getValue()
                )).sorted(Comparator.comparing(Three::getValue)).collect(Collectors.toList());
        threeSet.forEach(dwe -> System.out.println(dwe.getEdge() +" \t cap: "+(dwe.getValue()-8)+"\t inital:"+(initial.get(dwe.getEdge())+20)+" \t count: "+dwe.getCount()));
        Scanner scanner = new Scanner(System.in);
        int outline = scanner.nextInt();
        System.out.println("modifing network");
        List<DefaultWeightedEdge> modify = edgeDictionary.keySet().stream().filter(dwe -> edgeDictionary.get(dwe) > outline ).collect(Collectors.toList());
        modify.forEach(dwe -> graph.setEdgeWeight(dwe,graph.getEdgeWeight(dwe)*2));

        repeatFlows(graph,flows,initial);
        cutHelper = new MinCutHelper<>(graph);
//        ArrayList<Double> minCutValuesAfter = new ArrayList<>();
//        Map<DefaultWeightedEdge,Integer> modified = new HashMap<>();
//        for (int i = 0; i < pairs.size(); i++) {
//            Pair<Integer> pair = pairs.get(i);
//            List<Set<DefaultWeightedEdge>> minCutsBetweenVertexes =
//                    cutHelper.getAllMinCuts2(pair.getV1(),pair.getV2());
//            minCutValuesAfter.add(cutHelper.getMaxFlow());
//            List<DefaultWeightedEdge> edges = minCutsBetweenVertexes.stream().flatMap(dwes -> dwes.stream()).collect(Collectors.toList());
//            for (DefaultWeightedEdge edge : edges) {
//                if (!modified.containsKey(edge)){
//                    modified.put(edge,1);
//                }
//                else {
//                    Integer oldValue = modified.get(edge);
//                    modified.replace(edge,oldValue,oldValue+1);
//                }
//            }
//        }
//        System.out.println("");
//        System.out.println("bandwidth");
//        graph.edgeSet().forEach(b -> System.out.print(graph.getEdgeWeight(b)+","));
//        System.out.println("");
//        System.out.println("min cut values after modification");
//        minCutValuesAfter.stream().mapToInt(value -> value.intValue()).limit(999).forEach(aDouble -> System.out.print(aDouble +","));
//        System.out.println("");
//        System.out.println("new min cuts");
//        modified.values().forEach(integer -> System.out.print(integer + ","));



        jgxAdapter = new JGraphXAdapter<>(graph);

        jgxAdapter.setEdgeLabelsMovable(false);
        setPreferredSize(DEFAULT_SIZE);
        mxGraphComponent component = new mxGraphComponent(jgxAdapter);
        component.setConnectable(false);
        mxGraphModel graphModel  = (mxGraphModel)component.getGraph().getModel();
        Collection<Object> cells =  graphModel.getCells().values();
        mxUtils.setCellStyles(component.getGraph().getModel(),
                cells.toArray(), mxConstants.STYLE_ENDARROW, mxConstants.NONE);
//instead of getContentPane().add(new mxGraphComponent(jgxAdapter));
        getContentPane().add(component);
        component.getGraph().setAllowDanglingEdges(false);
        getContentPane().add(component);
        resize(DEFAULT_SIZE);
        mxCircleLayout layout = new mxCircleLayout(jgxAdapter);

        // center the circle
        int radius = 100;
        layout.setX0((DEFAULT_SIZE.width / 2.0) - radius);
        layout.setY0((DEFAULT_SIZE.height / 2.0) - radius);
        layout.setRadius(radius);
        layout.setMoveCircle(true);

        layout.execute(jgxAdapter.getDefaultParent());

    }

    private List<Pair<Integer>> putFlows(DefaultUndirectedWeightedGraph<Integer,DefaultWeightedEdge> graph,
                          int flowsCount, Map<DefaultWeightedEdge,Double> initialEdges){
        DataSetWriter dataSetWriter = new DataSetWriter(initialEdges);

        List<Pair<Integer>> addedFlows = new ArrayList<>();

        List<Integer> vertexes = graph.vertexSet().stream().collect(Collectors.toList());
        Random random = new Random();
        int index = 0;
        Map<DefaultWeightedEdge,Double> zeroEdges = new HashMap<>();
        for (int i = 0; i < flowsCount; i++) {
            System.out.println(i+" "+ zeroEdges.size() );
            DijkstraShortestPath<Integer,DefaultWeightedEdge> pathAlgo = new DijkstraShortestPath<>(new AsUndirectedGraph(graph));
            index = random.nextInt(vertexes.size());
            int sourse = vertexes.get(index);
            index = random.nextInt(vertexes.size());
            int end = vertexes.get(index);
            if (sourse == end){
                i-=1;
                vertexes.add(sourse);
                vertexes.add(end);
            }
            System.out.println(i+" source "+sourse+" sink "+end+" ");

            try {
                List<DefaultWeightedEdge> path = pathAlgo.getPath(sourse,end).getEdgeList();
                if (path.stream().anyMatch(dwe -> graph.getEdgeWeight(dwe) == 0 )){
                    path.stream().filter(dwe -> graph.getEdgeWeight(dwe) == 0)
                            .forEach(dwe -> {
                                zeroEdges.put(dwe,0.0);
                                graph.setEdgeWeight(dwe,Integer.MAX_VALUE);
                            });
                    pathAlgo = new DijkstraShortestPath<>(graph);
                    path = pathAlgo.getPath(sourse,end).getEdgeList();
                }
                path.forEach(dWE -> System.out.print(graph.getEdgeWeight(dWE)+ " ") );
                System.out.println();
                path.forEach(dWE -> graph.setEdgeWeight(dWE,graph.getEdgeWeight(dWE)-1));
                path.forEach(dWE -> System.out.print(graph.getEdgeWeight(dWE)+ " ") );
                addedFlows.add(new Pair(sourse,end));
                if (i%3 == 0 ){
                    dataSetWriter.WriteDataSet(graph,i);
                }
                System.out.println();
            }
            catch (NullPointerException e){
                i-=1;
                vertexes.add(sourse);
                vertexes.add(end);
            }
        }
        zeroEdges.forEach((defaultWeightedEdge, aDouble) -> graph.setEdgeWeight(defaultWeightedEdge,aDouble));
        return addedFlows;
    }

    private void repeatFlows(DefaultUndirectedWeightedGraph<Integer,DefaultWeightedEdge> graph,
                                         List<Pair<Integer>> addedFlows, Map<DefaultWeightedEdge,Double> initialEdges){
        DataSetWriter dataSetWriter = new DataSetWriter(initialEdges);
        Map<DefaultWeightedEdge,Double> zeroEdges = new HashMap<>();
        System.out.println("put flows on graph on another ways");
        for (int i = 0; i < addedFlows.size(); i++) {
            System.out.println(i+" "+ zeroEdges.size() );
            DijkstraShortestPath<Integer,DefaultWeightedEdge> pathAlgo = new DijkstraShortestPath<>(new AsUndirectedGraph(graph));
            Pair<Integer> indexes = addedFlows.get(i);
            addedFlows.remove(i);

            int sourse = indexes.getV1();
            int end = indexes.getV2();
            System.out.println(i+" source "+sourse+" sink "+end+" ");

                List<DefaultWeightedEdge> path = pathAlgo.getPath(sourse,end).getEdgeList();
                if (path.stream().anyMatch(dwe -> graph.getEdgeWeight(dwe) == 0 )){
                    path.stream().filter(dwe -> graph.getEdgeWeight(dwe) == 0)
                            .forEach(dwe -> {
                                zeroEdges.put(dwe,0.0);
                                graph.setEdgeWeight(dwe,Integer.MAX_VALUE);
                            });
                    pathAlgo = new DijkstraShortestPath<>(graph);
                    path = pathAlgo.getPath(sourse,end).getEdgeList();
                }
                path.forEach(dWE -> System.out.print(graph.getEdgeWeight(dWE)+ " ") );
                System.out.println();
                path.forEach(dWE -> graph.setEdgeWeight(dWE,graph.getEdgeWeight(dWE)-1));
                path.forEach(dWE -> System.out.print(graph.getEdgeWeight(dWE)+ " ") );
                addedFlows.add(new Pair(sourse,end));
                if (i%3 == 0 ){
                    dataSetWriter.WriteDataSet(graph,i);
                }
                System.out.println();
            }
        }

    private void InitializeGraph(DefaultUndirectedWeightedGraph<Integer, DefaultWeightedEdge> graph, TopologyType topologyType) {
        switch (topologyType) {
            case Tor2: InitializeTor(graph);
            break;
            case Dragonfly:InitializeDragonFly(graph,10,4);
            break;
            case Tor3:
            default:Initialize3DTor(graph,10);
        }
    }

    private void InitializeTor(DefaultUndirectedWeightedGraph<Integer, DefaultWeightedEdge> graph) {
        int size = 4;
        int count = size*size;
        int[][] gr = new int[size][size];
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                if (i==j) break;
                if (size % i == 1 ) gr[i][size*i] = 1;
                if (size % i != 0) gr[i][i+1] = 1;
                else gr[i][i- size +1] = 1;
                if (i+size > count) gr[i][i%size] = 1;
                else gr[i][i+size] = 1;
                if(i<size) gr[i][count-size+i] = 1;
                else gr[i][i-size] = 1;
            }
        }
        for (int i = 0; i < count; i++) {
            graph.addVertex(i);
        }
        for (int i = 0; i < count; i++) {
            for (int j = 0; j < count; j++) {
                if (gr[i][j] == 1 )graph.addEdge(i,j);
            }
        }
    }

    private void Initialize3DTor(DefaultUndirectedWeightedGraph<Integer, DefaultWeightedEdge> graph ,
                                 int size){
        for (int i = 0; i < size*size*size; i++) {
            graph.addVertex(i);// добавить вершину
        }

        int highLevel = size*size*size - size * size;// номер с которого идет верхний уровень
        for (int i = 0; i <size; i++) { // номер решетки
            for (int j = i*size*size; j < (i+1)*size*size*size; j++) { // номера вершин в решетке
                if (j % size != 0 && j< size*size*size -1 ) graph.addEdge(j,j+1);// ребро вперед в решетке
                else if(j< size*size*size -1) graph.addEdge(j,j+size-1);// ребро между краями решетки

                if (j != 0 && size*size/j > 2) graph.addEdge(j,j+size);// ребро влево по решетке
                else if(j>size*size && j< size*size*size -1) graph.addEdge(j,j-(size-1)*size);// ребро между краями решетки

                if (j < highLevel) graph.addEdge(j,j + size*size); // ребро в другую решетку
                else if (j< size*size*size -1) graph.addEdge(j , j - highLevel+1); // соединение верхней и нижней грани


            }
        }
    }

    private void InitializeDragonFly(DefaultUndirectedWeightedGraph<Integer, DefaultWeightedEdge> graph ,
                                     int groups ,int count) {
        int allHosts = groups * count;
        for (int i = 0; i < allHosts; i++) {
            graph.addVertex(i);
        }
        //local links and intergroup connections
        for (int i = 0; i < allHosts; i++) {
            graph.addEdge(i,i+1);
            if (i%1 == 1 && (i+count) < allHosts){
                graph.addEdge(i,i+2);
                graph.addEdge(i,i+ count - 1 );
            }
            if (i%1 == 2 && (i+count) < allHosts){
                graph.addEdge(i,i+ count - 2 );
            }
        }
        //global links
        for (int i = 0; i < allHosts; i++) {
            if (i%count == 0){graph.addEdge(i,i+count);}
            if (i% count > 1 ) {
                graph.addEdge(i,i+2* count -1);
                graph.addEdge(i,i+3* count -1);
            }
        }
    }


}
