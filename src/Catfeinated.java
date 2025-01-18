import java.util.ArrayList;
import java.util.Iterator;
import java.util.NoSuchElementException;

public class Catfeinated implements Iterable<Cat> {
    public CatNode root;

    public Catfeinated() {
    }


    public Catfeinated(CatNode dNode) {
        this.root = dNode;
    }

    // Constructor that makes a shallow copy of a Catfeinated cafe
    // New CatNode objects, but same Cat objects
    public Catfeinated(Catfeinated cafe) {
        if (cafe.root != null) {
            this.root = copyTree(cafe.root, null);
        }
        else {
            this.root = null;
        }
    }

    // Recursive helper method to copy a tree
    private CatNode copyTree(CatNode original, CatNode parent) {
        // Base Case
        if (original == null) {
            return null;
        }

        // Create a new CatNode with same Cat object
        CatNode newNode = new CatNode(original.catEmployee);
        newNode.parent = parent;

        // Recursively copy the senior and junior subtrees
        newNode.senior = copyTree(original.senior, newNode);
        newNode.junior = copyTree(original.junior, newNode);

        return newNode;
    }


    // add a cat to the cafe database
    public void hire(Cat c) {
        if (root == null)
            root = new CatNode(c);
        else
            root = root.hire(c);
    }

    // removes a specific cat from the cafe database
    public void retire(Cat c) {
        if (root != null)
            root = root.retire(c);
    }

    // get the oldest hire in the cafe
    public Cat findMostSenior() {
        if (root == null)
            return null;

        return root.findMostSenior();
    }

    // get the newest hire in the cafe
    public Cat findMostJunior() {
        if (root == null)
            return null;

        return root.findMostJunior();
    }

    // returns a list of cats containing the top numOfCatsToHonor cats
    // in the cafe with the thickest fur. Cats are sorted in descending
    // order based on their fur thickness.
    public ArrayList<Cat> buildHallOfFame(int numOfCatsToHonor) {
        ArrayList<Cat> hallOfFame = new ArrayList<>();
        if (root == null || numOfCatsToHonor <= 0) {
            return hallOfFame;
        }

        // temp array to do depth-first pre-order traversal
        ArrayList<CatNode> temp = new ArrayList<>();

        // Depth-first pre-order traversal of the tree
        temp.add(this.root);

        while (!temp.isEmpty() && hallOfFame.size() < numOfCatsToHonor) {
            CatNode current = temp.removeFirst();
            hallOfFame.add(current.catEmployee);

            if (current.senior != null) {
                temp.add(current.senior);
            }
            if (current.junior != null) {
                temp.add(current.junior);
            }
            // Sort the list with ArrayList's sort method
            // Got this idea from w3schools.com
            temp.sort((a, b) -> b.catEmployee.getFurThickness() - a.catEmployee.getFurThickness());
        }

        return hallOfFame;
    }


    // Returns the expected grooming cost the cafe has to incur in the next numDays days
    public double budgetGroomingExpenses(int numDays) {
        // Get the grooming schedule
        ArrayList<ArrayList<Cat>> groomingSchedule = getGroomingSchedule();

        double totalExpenses = 0;

        // Iterate through each week in the grooming schedule
        for (int week = 0; week < groomingSchedule.size(); week++) {
            // Iterate through the cats in the current week
            for (Cat cat : groomingSchedule.get(week)) {
                int daysToGrooming = cat.getDaysToNextGrooming();

                if (daysToGrooming <= numDays) {
                    totalExpenses += cat.getExpectedGroomingCost();
                }
            }
        }
        return totalExpenses;
    }


    // returns a list of list of Cats.
    // The cats in the list at index 0 need be groomed in the next week.
    // The cats in the list at index i need to be groomed in i weeks.
    // Cats in each sublist are listed in from most senior to most junior.
    public ArrayList<ArrayList<Cat>> getGroomingSchedule() {
        ArrayList<ArrayList<Cat>> groomingSchedule = new ArrayList<>();

        // Get iterable tree for in-order traversal
        CatfeinatedIterator iterator = new CatfeinatedIterator();

        makeSchedule(this.root, groomingSchedule);

        return groomingSchedule;
    }

    // Helper method to recursively traverse (in-order) the tree and populate groomingSchedule
    private void makeSchedule(CatNode node, ArrayList<ArrayList<Cat>> groomingSchedule) {
        // Base Case
        if (node == null) {
            return;
        }

        // Recursive Step : Junior
        makeSchedule(node.junior, groomingSchedule);

        // "Visit" current node
        Cat currentCat = node.catEmployee;
        int weekIndex = currentCat.getDaysToNextGrooming() / 7;

        // Make sure the groomingSchedule has enough sub-arrays for the weekIndex
        while (groomingSchedule.size() <= weekIndex) {
            groomingSchedule.add(new ArrayList<>());
        }

        // Add currentCat to the right list
        groomingSchedule.get(weekIndex).add(currentCat);

        // Recursive Step: Senior
        makeSchedule(node.senior, groomingSchedule);
    }


    public Iterator<Cat> iterator() {
        return new CatfeinatedIterator();
    }


    public static class CatNode {
        public Cat catEmployee;
        public CatNode junior;
        public CatNode senior;
        public CatNode parent;

        public CatNode(Cat c) {
            this.catEmployee = c;
            this.junior = null;
            this.senior = null;
            this.parent = null;
        }

        // add the c to the tree rooted at this and returns the root of the resulting tree
        public CatNode hire (Cat c) {
            // if c is more senior, goes to the left
            if (c.compareTo(this.catEmployee) > 0) {
                // If no senior, put in senior
                if (this.senior == null){
                    this.senior = new CatNode(c);
                    this.senior.parent = this;
                }
                // If senior, keep going
                else {
                    this.senior = this.senior.hire(c);
                }
            }
            // c is more junior, goes to the right
            else {
                // If no junior, put in junior
                if (this.junior == null) {
                    this.junior = new CatNode(c);
                    this.junior.parent = this;
                }
                // If junior, keep going
                else {
                    this.junior = this.junior.hire(c);
                }
            }
            // Fix the tree based on Max-Heap, using rotations
            if (this.senior != null && this.senior.catEmployee.getFurThickness() > this.catEmployee.getFurThickness()) {
                return this.rightRotate();
            }
            if (this.junior != null && this.junior.catEmployee.getFurThickness() > this.catEmployee.getFurThickness()) {
                return this.leftRotate();
            }
            return this;
        }


        // Helper method to right rotate
        private CatNode rightRotate() {
            CatNode leftChild = this.senior;
            // Transfer leftChild's right subtree
            this.senior = leftChild.junior;
            if (this.senior != null) {
                this.senior.parent = this;
            }
            // Promote leftChild to root
            leftChild.junior = this;
            leftChild.parent = this.parent;
            this.parent = leftChild;

            return leftChild;
        }


        // Helper method to left rotate
        private CatNode leftRotate() {
            CatNode rightChild = this.junior;
            // Transfer rightChild's right subtree
            this.junior = rightChild.senior;
            if (this.junior != null) {
                this.junior.parent = this;
            }
            // Promote rightChild to "root"
            rightChild.senior = this;
            rightChild.parent = this.parent;
            this.parent = rightChild;

            return rightChild;
        }


        // remove c from the tree rooted at this and returns the root of the resulting tree
        public CatNode retire(Cat c) {
            // If c is older
            if (c.compareTo(this.catEmployee) > 0) {
                if (this.senior != null) {
                    this.senior = this.senior.retire(c);
                }
            }
            // If c is younger
            else if (c.compareTo(this.catEmployee) < 0) {
                if (this.junior != null) {
                    this.junior = this.junior.retire(c);
                }
            }
            // If node to be removed is found
            else if (c.equals(this.catEmployee)) {
                // If this node has no senior
                if (this.senior == null) {
                    return this.junior;
                }
                // If this node has no junior
                else if (this.junior == null) {
                    return this.senior;
                }
                // If this node has both children
                else {
                    // Find the most senior cat in the senior subtree
                    CatNode seniorCNode = this.senior;
                    while (seniorCNode.senior != null) {
                        seniorCNode = seniorCNode.senior;
                    }
                    this.catEmployee = seniorCNode.catEmployee;
                    this.senior = this.senior.retire(this.catEmployee);
                }
            }

            // Fix the tree based on Max-Heap, using rotations
            return this.downHeap();
        }

        // Helper method to fix Max-Heap for retire
        private CatNode downHeap() {
            CatNode thickest = this;

            // Check senior
            if (this.senior != null && this.senior.catEmployee.getFurThickness() > thickest.catEmployee.getFurThickness()) {
                thickest = this.senior;
            }

            // Check junior
            if (this.junior != null && this.junior.catEmployee.getFurThickness() > thickest.catEmployee.getFurThickness()) {
                thickest = this.junior;
            }

            // Check if rotation needed
            if (thickest != this) {
                if (thickest == this.senior) {
                    return this.rightRotate();
                }
                else if (thickest == this.junior) {
                    return this.leftRotate();
                }
            }

            // If no rotation needed
            return this;
        }

        // find the cat with highest seniority in the tree rooted at this
        public Cat findMostSenior() {
            // Temp CatNode to iterate through nodes
            CatNode	curr = this;

            // Loop over all the older nodes
            while(curr.senior != null) {
                curr = curr.senior;
            }

            return curr.catEmployee;
        }

        // find the cat with lowest seniority in the tree rooted at this
        public Cat findMostJunior() {
            // Temp CatNode to iterate through nodes
            CatNode	curr = this;

            // Loop over all the younger nodes
            while(curr.junior != null) {
                curr = curr.junior;
            }

            return curr.catEmployee;
        }

        // Feel free to modify the toString() method if you'd like to see something else displayed.
        public String toString() {
            String result = this.catEmployee.toString() + "\n";
            if (this.junior != null) {
                result += "junior than " + this.catEmployee.toString() + " :\n";
                result += this.junior.toString();
            }
            if (this.senior != null) {
                result += "senior than " + this.catEmployee.toString() + " :\n";
                result += this.senior.toString();
            } /*
			if (this.parent != null) {
				result += "parent of " + this.catEmployee.toString() + " :\n";
				result += this.parent.catEmployee.toString() +"\n";
			}*/
            return result;
        }
    }


    public class CatfeinatedIterator implements Iterator<Cat> {
        private ArrayList<Cat> cats;
        private int currentIndex;

        public CatfeinatedIterator() {
            cats = new ArrayList<>();
            currentIndex = 0;

            if (root != null) {
                inOrderTraversal(root);
            }
        }

        // Recursive in-order traversal to collect cats in ascending order
        private void inOrderTraversal(CatNode node) {
            // Base Case
            if (node == null) {
                return;
            }

            // Recursive step: senior
            inOrderTraversal(node.senior);

            // Add current node
            cats.add(node.catEmployee);

            // Recursive step: junior
            inOrderTraversal(node.junior);
        }

        public Cat next(){
            if (!this.hasNext()) {
                throw new NoSuchElementException("No more cats in the iterator.");
            }
            return cats.get(currentIndex++);
        }

        public boolean hasNext() {
            return currentIndex < cats.size();
        }

    }

    public static void main(String[] args) {
        Cat B = new Cat("Buttercup", 45, 53, 5, 85.0);
        Cat C = new Cat("Chessur", 8, 23, 2, 250.0);
        Cat J = new Cat("Jonesy", 0, 21, 12, 30.0);
        Cat JJ = new Cat("JIJI", 156, 17, 1, 30.0);
        Cat JTO = new Cat("J. Thomas O'Malley", 21, 10, 9, 20.0);
        Cat MrB = new Cat("Mr. Bigglesworth", 71, 0, 31, 55.0);
        Cat MrsN = new Cat("Mrs. Norris", 100, 68, 15, 115.0);
        Cat T = new Cat("Toulouse", 180, 37, 14, 25.0);

        Catfeinated cafe = new Catfeinated();
        cafe.hire(C);
        System.out.println(cafe.root);
        cafe.hire(J);
        System.out.println(cafe.root);
        cafe.hire(JJ);
        System.out.println(cafe.root);
        cafe.hire(JTO);
        System.out.println(cafe.root);
        cafe.hire(MrB);
        System.out.println(cafe.root);
        cafe.hire(MrsN);
        System.out.println(cafe.root);
        cafe.hire(B);
        System.out.println(cafe.root);
        cafe.hire(T);
        System.out.println(cafe.root);

        ArrayList<Cat> hallOfFame = cafe.buildHallOfFame(5);
        System.out.println(hallOfFame);

        Cat BC = new Cat("Blofeld's cat", 6, 72, 18, 120.0);
        Cat L = new Cat("Lucifer", 10, 44, 20, 50.0);

    }


}


