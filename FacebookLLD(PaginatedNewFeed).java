import java.util.*;

public class FacebookDesign {
    private static class Post {
        int userId;
        long timestamp;

        Post(int userId, long timestamp) {
            this.userId = userId;
            this.timestamp = timestamp;
        }
    }

    private static class PostIterator {
        int postId;
        long timestamp;
        int followeeId;
        int nextIndex;

        PostIterator(int postId, long timestamp, int followeeId, int nextIndex) {
            this.postId = postId;
            this.timestamp = timestamp;
            this.followeeId = followeeId;
            this.nextIndex = nextIndex;
        }

        public long getTimestamp() {
            return timestamp;
        }
    }

    private Map<Integer, List<Integer>> userPosts;
    private Map<Integer, Post> postStore;
    private Map<Integer, Set<Integer>> followMap;
    private static final int PAGE_SIZE = 10;

    public FacebookDesign() {
        userPosts = new HashMap<>();
        postStore = new HashMap<>();
        followMap = new HashMap<>();
    }

    public void post(int userId, int postId) {
        userPosts.putIfAbsent(userId, new ArrayList<>());
        userPosts.get(userId).add(0, postId); // Insert at the beginning for newest first
        postStore.put(postId, new Post(userId, System.currentTimeMillis()));
    }

    public void follow(int followerId, int followeeId) {
        followMap.putIfAbsent(followerId, new HashSet<>());
        followMap.get(followerId).add(followeeId);
    }

    public void unfollow(int followerId, int followeeId) {
        if (followMap.containsKey(followerId)) {
            followMap.get(followerId).remove(followeeId);
        }
    }

    public List<Integer> getNewsFeed(int userId) {
        return getNewsFeedPaginated(userId, 0);
    }

    public List<Integer> getNewsFeedPaginated(Integer userId, Integer pageNumber) {
        Set<Integer> followees = new HashSet<>(followMap.getOrDefault(userId, new HashSet<>()));
        followees.add(userId); // Include self

        PriorityQueue<PostIterator> heap = new PriorityQueue<>(
            (a, b) -> Long.compare(b.getTimestamp(), a.getTimestamp())
        );

        // Initialize heap with the first valid post from each followee
        for (int followee : followees) {
            List<Integer> posts = userPosts.getOrDefault(followee, new ArrayList<>());
            int index = 0;
            while (index < posts.size()) {
                int postId = posts.get(index);
                if (postStore.containsKey(postId)) {
                    Post post = postStore.get(postId);
                    heap.offer(new PostIterator(postId, post.timestamp, followee, index + 1));
                    break;
                }
                index++;
            }
        }

        List<Integer> allPosts = new ArrayList<>();
        while (!heap.isEmpty()) {
            PostIterator current = heap.poll();
            allPosts.add(current.postId);

            // Get next post from the same followee
            List<Integer> posts = userPosts.getOrDefault(current.followeeId, new ArrayList<>());
            int nextIndex = current.nextIndex;
            while (nextIndex < posts.size()) {
                int postId = posts.get(nextIndex);
                if (postStore.containsKey(postId)) {
                    Post post = postStore.get(postId);
                    heap.offer(new PostIterator(postId, post.timestamp, current.followeeId, nextIndex + 1));
                    break;
                }
                nextIndex++;
            }
        }

        int start = pageNumber * PAGE_SIZE;
        int end = start + PAGE_SIZE;
        if (start >= allPosts.size()) {
            return Collections.emptyList();
        }
        end = Math.min(end, allPosts.size());
        return allPosts.subList(start, end);
    }

    public void deletePost(int postId) {
        postStore.remove(postId);
    }
}
