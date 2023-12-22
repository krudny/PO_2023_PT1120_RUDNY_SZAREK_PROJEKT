package agh.ics.oop.project.model;
import agh.ics.oop.project.interfaces.Comparator;
import agh.ics.oop.project.model.AnimalComparator;
import agh.ics.oop.project.interfaces.Map;
import agh.ics.oop.project.interfaces.WorldElement;
import agh.ics.oop.project.model.util.MapVisualizer;

import java.util.*;

public class WorldMap implements Map{
    private final Boundary boundary;
    private final int height;
    private final int width;
    private HashMap<Vector2d, Tile> tiles;
    private final int id;

    private final MapVisualizer mapVisualizer = new MapVisualizer(this);

    public WorldMap(int width, int height, int id) {
        this.height = height;
        this.width = width;
        this.boundary = new Boundary(new Vector2d(0, 0), new Vector2d(width - 1, height - 1));
        this.id=id;
        this.tiles = new HashMap<>();
    }

    @Override
    public boolean placeAnimal(Animal animal) {
        Vector2d pos = animal.getPosition();
        if (canMoveTo(pos)) {
            if (!tiles.containsKey(pos)) tiles.put(pos, new Tile());
            tiles.get(pos).addAnimal(animal);
            return true;
        }
        return false;
    }

    public void removeAnimal(Animal animal){
        tiles.get(animal.getPosition()).removeAnimal(animal);
    }

    @Override
    public boolean placePlant(Plant plant) {
        Vector2d pos = plant.getPosition();
        if (!tiles.containsKey(pos) || tiles.get(pos).getPlant()==null) {
            if (!tiles.containsKey(pos)) tiles.put(pos, new Tile());
            tiles.get(pos).setPlant(plant);
            return true;
        }
        return false;
    }

    public void increaseAge(Animal animal){
        animal.setAge(animal.getAge() + 1);
    }

    public void decreaseEnergy(Animal animal){
        animal.setEnergy(animal.getEnergy() - 1);
    }

    public void moveAnimals(){
        Set<Animal> moved = new HashSet<>();

        for (Animal animal : getAnimals()){
            if (!moved.contains(animal)) {
                Vector2d old_pos=animal.getPosition();
                Tile t = tiles.get(old_pos);

                // move the animal
                t.removeAnimal(animal);
                animal.move(this);

                // create Tile on Vector2d if doesnt exist
                if (!tiles.containsKey(animal.getPosition())) {
                    tiles.put(animal.getPosition(), new Tile());
                }

                // put animal on the new tile
                tiles.get(animal.getPosition()).addAnimal(animal);
                moved.add(animal);

                increaseAge(animal);
                decreaseEnergy(animal);
                deleteIfEmpty(old_pos);
            }
        }
    }

    public void eatPlants() {
        for(Vector2d position : tiles.keySet()) {
            Tile t = tiles.get(position);
            if(t.getPlant() != null && !t.getAnimals().isEmpty()) {
                List<Animal> animals = t.getAnimals();

                Animal max_animal = animals.get(0);
                AnimalComparator comparator = new AnimalComparator();

                for(Animal animal : animals){
                    if (comparator.compare(max_animal, animal) > 0){
                        max_animal = animal;
                    }
                }

                int max_energy = max_animal.getEnergy();

                t.removeAnimal(max_animal);
                max_animal.setEnergy(max_energy + t.getPlant().getEnergy());
                t.addAnimal(max_animal);
                t.removePlant();

                deleteIfEmpty(position);
            }
        }
    }


    public void deleteIfEmpty(Vector2d position){
        if (tiles.containsKey(position)){
            Tile t = tiles.get(position);
            if (t.isEmpty()) {
                tiles.remove(position);
            }
        }
    }
    @Override
    public List<WorldElement> objectsAt(Vector2d position) {
        Tile t = tiles.get(position);
        if (t == null) {
            return new ArrayList<>();
        }
        List<WorldElement> result = new ArrayList<>(t.getAnimals());
        if (t.getPlant() != null) {
            result.add(t.getPlant());
        }
        return result;
    }

    @Override
    public boolean isOccupied(Vector2d position) {
        return tiles.get(position) != null;
    }

    public Plant getPlant(Vector2d pos){
        if (isOccupied(pos)){
            return tiles.get(pos).getPlant();
        }
        return null;
    }
    @Override
    public List<WorldElement> getElements() {
        List<WorldElement> result = new ArrayList<>();
        for (Tile t : tiles.values()) {
            result.addAll(t.getAnimals());
            if (t.getPlant() != null) result.add(t.getPlant());
        }
        return result;
    }

    public List<Animal> getAnimals(){
        List<Animal> result = new ArrayList<>();
        for (Tile t : tiles.values()) {
            result.addAll(t.getAnimals());
        }
        return result;
    }
    @Override
    public int getHeight() {
        return height;
    }

    @Override
    public int getWidth() {
        return width;
    }

    @Override
    public boolean canMoveTo(Vector2d position) {
        // domyslnie true - dla wody bedzie false
        return true;
    }

    @Override
    public Boundary getBoundary() {
        return boundary;
    }

    public int getId() {
        return id;
    }

    @Override
    public String toString() {
        return mapVisualizer.draw(boundary.lower_left(), boundary.upper_right());
    }


}
