package helpers;
public class Pair<V> {
    private V v1;
    private V v2;

    public Pair(V v1, V v2){
        setV1(v1);
        setV2(v2);
    }

    public V getV2() {
        return v2;
    }

    public void setV2(V v2) {
        this.v2 = v2;
    }

    public V getV1() {
        return v1;
    }

    public void setV1(V v1) {
        this.v1 = v1;
    }

    @Override
    public String toString() {
        return v1.toString() + " " + v2.toString();
    }
}
