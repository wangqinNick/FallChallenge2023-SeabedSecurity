import java.util.*;
import java.util.stream.Collectors;

// Define the data structures as records
record Vector(int x, int y) {
}

record FishDetail(int color, int type) {
}

record Fish(int fishId, Vector pos, Vector speed, FishDetail detail) {
}

record Drone(int droneId, Vector pos, boolean dead, int battery, List<Integer> scans, int light) {
}

record RadarBlip(int fishId, String dir) {
}

class Player {
    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);

        Map<Integer, FishDetail> fishDetails = new HashMap<>();

        List<Integer> scannedIds = new ArrayList<>();

        int fishCount = in.nextInt();
        for (int i = 0; i < fishCount; i++) {
            int fishId = in.nextInt();
            int color = in.nextInt();
            int type = in.nextInt();
            fishDetails.put(fishId, new FishDetail(color, type));
        }

        // game loop
        while (true) {
            List<Integer> myScans = new ArrayList<>();
            List<Integer> foeScans = new ArrayList<>();
            Map<Integer, Drone> droneById = new HashMap<>();
            List<Drone> myDrones = new ArrayList<>();   // 我方无人机
            List<Drone> foeDrones = new ArrayList<>();  // 敌方无人机
            List<Fish> visibleFishes = new ArrayList<>();
            Map<Integer, List<RadarBlip>> myRadarBlips = new HashMap<>();

            int myScore = in.nextInt();
            int foeScore = in.nextInt();

            int myScanCount = in.nextInt();
            for (int i = 0; i < myScanCount; i++) {
                int fishId = in.nextInt();
                myScans.add(fishId);
            }

            int foeScanCount = in.nextInt();
            for (int i = 0; i < foeScanCount; i++) {
                int fishId = in.nextInt();
                foeScans.add(fishId);
            }

            int myDroneCount = in.nextInt();
            for (int i = 0; i < myDroneCount; i++) {
                int droneId = in.nextInt();
                int droneX = in.nextInt();
                int droneY = in.nextInt();
                boolean dead = in.nextInt() == 1;
                int battery = in.nextInt();
                Vector pos = new Vector(droneX, droneY);
                Drone drone = new Drone(droneId, pos, dead, battery, new ArrayList<>(), 0);
                droneById.put(droneId, drone);
                myDrones.add(drone);
                myRadarBlips.put(droneId, new ArrayList<>());
            }

            int foeDroneCount = in.nextInt();
            for (int i = 0; i < foeDroneCount; i++) {
                int droneId = in.nextInt();
                int droneX = in.nextInt();
                int droneY = in.nextInt();
                boolean dead = in.nextInt() == 1;
                int battery = in.nextInt();
                Vector pos = new Vector(droneX, droneY);
                Drone drone = new Drone(droneId, pos, dead, battery, new ArrayList<>(), 0);
                droneById.put(droneId, drone);
                foeDrones.add(drone);
            }

            int droneScanCount = in.nextInt();
            for (int i = 0; i < droneScanCount; i++) {
                int droneId = in.nextInt();
                int fishId = in.nextInt();
                droneById.get(droneId).scans().add(fishId);
            }

            int visibleFishCount = in.nextInt();
            for (int i = 0; i < visibleFishCount; i++) {
                int fishId = in.nextInt();
                int fishX = in.nextInt();
                int fishY = in.nextInt();
                int fishVx = in.nextInt();
                int fishVy = in.nextInt();
                Vector pos = new Vector(fishX, fishY);
                Vector speed = new Vector(fishVx, fishVy);
                FishDetail detail = fishDetails.get(fishId);
                visibleFishes.add(new Fish(fishId, pos, speed, detail));
            }

            int myRadarBlipCount = in.nextInt();
            for (int i = 0; i < myRadarBlipCount; i++) {
                int droneId = in.nextInt();
                int fishId = in.nextInt();
                String radar = in.next();
                myRadarBlips.get(droneId).add(new RadarBlip(fishId, radar));
            }

            for (Drone drone : myDrones) {
                // 处理未找到雷达脉冲信息列表的情况
                int targetX = drone.pos().x();
                int targetY = drone.pos().y();


                int currentX = drone.pos().x();
                int currentY = drone.pos().y();
                // TODO: Implement logic on where to move here
                System.err.println("当前可见鱼: " + visibleFishes);
                System.err.println("当前已经扫描的鱼: " + scannedIds);

                // 找到最近的未被扫描的鱼
                Fish nearestFish = findNearestFish(drone, visibleFishes, scannedIds);

                // 如果找到未被扫描的最近鱼，更新目标位置
                if (nearestFish != null) {
                    targetX = nearestFish.pos().x();
                    targetY = nearestFish.pos().y();
                }

                System.err.println("当前目标鱼: " + nearestFish);
                System.out.printf("MOVE %d %d %d%n", targetX, targetY, drone.light());

            }

            // 游戏回合结束后更新自动扫描到的鱼
            for (Drone drone : myDrones) {
                // 自动扫描的半径
                double autoScanRadius = (drone.light() == 1) ? 800 - 600 : 2000 - 600;

                List<Integer> autoScannedFishIds = visibleFishes.stream()
                        .filter(fish -> !scannedIds.contains(fish.fishId()) && calculateDistance(drone.pos(), fish.pos()) <= autoScanRadius)
                        .map(Fish::fishId)
                        .toList();


                // 添加自动扫描到的鱼到已扫描列表
                scannedIds.addAll(autoScannedFishIds);
            }

        }
    }

    // 计算两点距离
    private static double calculateDistance(Vector point1, Vector point2) {
        return Math.sqrt(Math.pow(point1.x() - point2.x(), 2) + Math.pow(point1.y() - point2.y(), 2));
    }

    // 找到最近的未被扫描的鱼
    private static Fish findNearestFish(Drone drone, List<Fish> visibleFishes, List<Integer> scannedIds) {
        double minDistance = Double.MAX_VALUE;
        Fish nearestFish = null;

        for (Fish fish : visibleFishes) {
            // 检查当前鱼是否已经被扫描过
            if (!scannedIds.contains(fish.fishId())) {
                double distance = calculateDistance(drone.pos(), fish.pos());

                // 如果距离比当前最小距离小，则更新最小距离和目标鱼
                if (distance < minDistance) {
                    minDistance = distance;
                    nearestFish = fish;
                }
            }
        }

        return nearestFish;
    }

    // 以下是判断是否需要返回基地的函数
    private static boolean shouldResurface(Drone drone, Vector basePosition) {
        // 根据你的游戏规则判断是否需要返回基地
        // 例如，可以在电池低于某个阈值时返回基地
        return drone.battery() < 5 || drone.scans().size() < 4;
    }
}

