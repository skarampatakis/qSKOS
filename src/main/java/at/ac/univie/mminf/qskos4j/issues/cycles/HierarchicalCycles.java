package at.ac.univie.mminf.qskos4j.issues.cycles;

import at.ac.univie.mminf.qskos4j.issues.HierarchyGraph;
import at.ac.univie.mminf.qskos4j.issues.Issue;
import at.ac.univie.mminf.qskos4j.result.general.CollectionResult;
import at.ac.univie.mminf.qskos4j.util.graph.NamedEdge;
import org.jgrapht.DirectedGraph;
import org.jgrapht.alg.CycleDetector;
import org.jgrapht.alg.StrongConnectivityInspector;
import org.openrdf.OpenRDFException;
import org.openrdf.model.Resource;
import org.openrdf.model.Value;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Created by christian
 * Date: 26.01.13
 * Time: 16:26
 *
 * Finds all <a href="https://github.com/cmader/qSKOS/wiki/Quality-Issues#wiki-Cyclic_Hierarchical_Relations">Cyclic Hierarchical Relations</a>.
 */
public class HierarchicalCycles extends Issue<CollectionResult<Set<Value>>> {

    private DirectedGraph<Value, NamedEdge> hierarchyGraph;

    public HierarchicalCycles() {
        super("chr",
              "Cyclic Hierarchical Relations",
              "Finds all hierarchy cycle containing components",
              IssueType.ANALYTICAL
        );
    }

    @Override
    protected CollectionResult<Set<Value>> invoke() throws OpenRDFException {
        hierarchyGraph = new HierarchyGraph(vocabRepository).createGraph();
        Set<Value> nodesInCycles = new CycleDetector<Value, NamedEdge>(hierarchyGraph).findCycles();
        List<Set<Value>> cycleContainingComponents = trackNodesInCycles(nodesInCycles);

        return new HierarchyCycleResult(cycleContainingComponents, hierarchyGraph);
    }

    private List<Set<Value>> trackNodesInCycles(Set<Value> nodesInCycles)
    {
        List<Set<Value>> ret = new ArrayList<Set<Value>>();
        List<Set<Value>> stronglyConnectedSets =
                new StrongConnectivityInspector<Value, NamedEdge>(hierarchyGraph).stronglyConnectedSets();

        for (Value node : nodesInCycles) {
            for (Set<Value> stronglyConnectedSet : stronglyConnectedSets) {
                if (stronglyConnectedSet.contains(node)) {
                    if (!ret.contains(stronglyConnectedSet)) {
                        ret.add(stronglyConnectedSet);
                    }
                }
            }
        }

        return ret;
    }
}