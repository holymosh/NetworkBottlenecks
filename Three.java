package helpers;

public class Three<E> {
    private E edge;
    private double value;
    private int count;

    public Three(E edge, double value, int count) {
        this.edge = edge;
        this.value = value;
        this.count = count;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }

    public E getEdge() {
        return edge;
    }

    public void setEdge(E edge) {
        this.edge = edge;
    }
}
