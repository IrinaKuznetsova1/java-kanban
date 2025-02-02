package service;

import model.Task;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InMemoryHistoryManager implements HistoryManager {

    private static class Node {
        Task task;
        Node prev;
        Node next;

        private Node(Node prev, Task task, Node next) {
            this.prev = prev;
            this.task = task;
            this.next = next;
        }
    }

    private final Map<Integer, Node> nodeMap = new HashMap<>();
    private Node head;
    private Node tail;

    private void linkLast(Task task) {
        final Node node = new Node(tail, task, null);
        if (tail == null)
            head = node;
        else
            tail.next = node;
        tail = node;
    }

    private ArrayList<Task> getTasks() {
        ArrayList<Task> tasks = new ArrayList<>();
        Node node = head;
        while(node != null) {
            tasks.add(node.task);
            node = node.next;
        }
        return tasks;
    }

    private void removeNode(Node node) {
        if (node.prev == null) {
            head = node.next;
            node.next.prev = null;
        } else if (node.next == null) {
            tail = node.prev;
            node.prev.next = null;
        } else {
            Node prevNode = node.prev;
            Node nextNode = node.next;
            prevNode.next = nextNode;
            nextNode.prev = prevNode;
        }
    }

    @Override
    public void addTask(Task task) {
        if (task == null) return;
        remove(task.getId());
        linkLast(task);
        final Node nodeTask = tail;
        nodeMap.put(task.getId(), nodeTask);

    }

    @Override
    public List<Task> getHistory() {
        return getTasks();
    }

    @Override
    public void remove(int id) {
        final Node node = nodeMap.remove(id);

        if (node == null) return;

        if (nodeMap.isEmpty()) {
            head = null;
            tail = null;
            return;
        }
        removeNode(node);
    }
}
