package com.algoviz.controller;

import com.algoviz.model.TreeNode;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

@RestController
@RequestMapping("/api/trees")
@CrossOrigin(origins = "http://localhost:5173")
public class TreeController {

    public static class BuildRequest {
        private String type;
        private List<Integer> values;

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public List<Integer> getValues() {
            return values;
        }

        public void setValues(List<Integer> values) {
            this.values = values;
        }
    }

    @PostMapping("/build")
    public TreeNode buildTree(@RequestBody BuildRequest request) {
        if (request.getType() == null ||
                request.getValues() == null ||
                request.getValues().size() > 100) {
            throw new IllegalArgumentException(
                    "Tree type and up to 100 values are required"
            );
        }

        String type = request.getType().toLowerCase();
        List<Integer> values = request.getValues();

        if (!type.equals("binary") && !type.equals("bst")) {
            throw new IllegalArgumentException(
                    "Type must be binary or bst"
            );
        }

        if (values.stream().anyMatch(value -> value == null)) {
            throw new IllegalArgumentException(
                    "Values cannot be null"
            );
        }

        if (type.equals("binary")) {
            return buildBinaryTree(values);
        }

        TreeNode root = null;

        for (int value : values) {
            root = insertBST(root, value);
        }

        return root;
    }

    private TreeNode buildBinaryTree(List<Integer> values) {
        if (values.isEmpty()) {
            return null;
        }

        TreeNode root = new TreeNode(values.get(0));
        Queue<TreeNode> queue = new LinkedList<>();
        queue.add(root);

        int i = 1;

        while (i < values.size()) {
            TreeNode current = queue.poll();

            current.left = new TreeNode(values.get(i++));
            queue.add(current.left);

            if (i < values.size()) {
                current.right = new TreeNode(values.get(i++));
                queue.add(current.right);
            }
        }

        return root;
    }

    private TreeNode insertBST(TreeNode root, int value) {
        if (root == null) {
            return new TreeNode(value);
        }

        if (value < root.value) {
            root.left = insertBST(root.left, value);
        } else if (value > root.value) {
            root.right = insertBST(root.right, value);
        }

        // Duplicate values are ignored in a BST.
        return root;
    }
}