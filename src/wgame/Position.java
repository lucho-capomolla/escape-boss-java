package wgame;

/**
 * Equivale a las "position" de Wollok Game (game.at(x,y)).
 * En Wollok: position, right(n), left(n), up(n), down(n), distance(otra)
 */
public final class Position {

    private final int x;
    private final int y;

    public Position(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int x() { return x; }
    public int y() { return y; }

    public Position right(int n) { return new Position(x + n, y); }
    public Position left(int n)  { return new Position(x - n, y); }
    public Position up(int n)    { return new Position(x, y + n); }
    public Position down(int n)  { return new Position(x, y - n); }

    public double distance(Position other) {
        int dx = x - other.x;
        int dy = y - other.y;
        return Math.sqrt(dx * dx + dy * dy);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Position)) return false;
        Position p = (Position) o;
        return p.x == x && p.y == y;
    }

    @Override
    public int hashCode() { return 31 * x + y; }

    @Override
    public String toString() { return "(" + x + ", " + y + ")"; }
}
