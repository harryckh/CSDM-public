package dk.aau.cs.idq.utilities;

import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import dk.aau.cs.idq.datagen.OTTGen;
import dk.aau.cs.idq.indoorentities.D2Ddistance;
import dk.aau.cs.idq.indoorentities.Door;
import dk.aau.cs.idq.indoorentities.IndoorSpace;
import dk.aau.cs.idq.indoorentities.Par;

public class D2D {

	private List<Door> oneFloorDoors;

	private List<Par> oneFloorPars;

	private SortedSet<D2Ddistance> min_heap;

	private int[] prev;

	private double[] dist;

	private int[] visited;
	
	public D2D(){
		oneFloorDoors = IndoorSpace.gDoors.subList(0,
				IndoorSpace.gNumberDoorsPerFloor);
		oneFloorPars = IndoorSpace.gPartitions.subList(0,
				IndoorSpace.gNumberParsPerFloor);
		min_heap = new TreeSet<D2Ddistance>();
		prev = new int[IndoorSpace.gNumberDoorsPerFloor];
		dist = new double[IndoorSpace.gNumberDoorsPerFloor];
		visited = new int[IndoorSpace.gNumberDoorsPerFloor];
	}

	public D2Ddistance d2dDistance(Door d_s, Door d_t) {

		int d_sID = d_s.getmID();
		int d_tID = d_t.getmID();
		

		for (Door d_i : oneFloorDoors) {
			int d_iID = d_i.getmID();

			if (d_iID != d_sID) {
				dist[d_iID] = Constant.DISTMAX;
			} else {
				dist[d_iID] = 0;
			}

			min_heap.add(new D2Ddistance(d_sID, d_iID, dist[d_iID]));
			prev[d_iID] = -1;

		}
		
		while (!min_heap.isEmpty()) {
			
			D2Ddistance d2dDist = min_heap.first();
			min_heap.remove(min_heap.first());
			
			int d_iID = d2dDist.getDoorID_2();
			Door d_i = this.oneFloorDoors.get(d_iID);
			
			if (d_iID == d_tID) {
				return d2dDist;
			}

			visited[d_iID] = 1;
			List<Integer> D2Pleave_di = d_i.getmPartitions();
			for (int parIDv : D2Pleave_di) {
				List<Integer> P2Dleave_parIDv = oneFloorPars.get(parIDv)
						.getmDoors();
				for (int d_jID : P2Dleave_parIDv) {
					if (visited[d_jID] != 1) {
						
						double dist_i_j;
						if (d_iID == d_jID) {
							dist_i_j = 0;
						} else {
							String key = Functions.keyConventer(d_iID, d_jID);
							if (IndoorSpace.gD2D.containsKey(key)) {
								dist_i_j = IndoorSpace.gD2D.get(key)
										.getDistance();
							} else {
								dist_i_j = Constant.DISTMAX;
							}
						}

						if (dist[d_iID] + dist_i_j < dist[d_jID]) {

							dist[d_jID] = dist[d_iID] + dist_i_j;
							for (D2Ddistance entry : min_heap) {
								if (entry.getDoorID_2() == d_jID) {
									min_heap.add(new D2Ddistance(d_sID, d_jID,
											dist[d_jID]));
									break;
								}
							}
							prev[d_jID] = d_iID;
						}

					}
				}
			}

		}

		return null;
	}

	public static void main(String[] args) {

		OTTGen ottGen = new OTTGen();
		ottGen.generateOTT(6,0);

		double [][]distMatrix = new double[IndoorSpace.gNumberParsPerFloor][IndoorSpace.gNumberParsPerFloor];
		
		for(int i = 0; i < IndoorSpace.gNumberParsPerFloor; i++){
			distMatrix[i][i] = 0;
			for(int j = i+1; j < IndoorSpace.gNumberParsPerFloor; j++){
				D2D d2d = new D2D();
				D2Ddistance result = d2d.d2dDistance(IndoorSpace.gDoors.get(i), IndoorSpace.gDoors.get(j));
				
				distMatrix[i][j] = result.getDistance();
				distMatrix[j][i] = result.getDistance();
			}
		}
		
	}

}
