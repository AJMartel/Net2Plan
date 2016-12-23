package com.net2plan.interfaces.networkDesign;

import com.net2plan.libraries.GraphUtils.ClosedCycleRoutingException;
import com.net2plan.utils.Constants.RoutingCycleType;
import com.net2plan.utils.Constants.RoutingType;
import com.net2plan.utils.Pair;
import org.junit.*;

import java.util.*;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class DemandTest 
{
	private NetPlan np = null;
	private Node n1, n2 , n3;
	private Link link12, link23 , link13;
	private Demand d13, d12 , scd123;
	private Route r12, r123a, r123b , sc123;
	private List<Link> path13;
	private List<NetworkElement> pathSc123;
	private Resource res2 , res2backup;
	private ProtectionSegment segm13;
	private NetworkLayer lowerLayer , upperLayer;
	private Link upperLink12;
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception 
	{
		this.np = new NetPlan ();
		this.lowerLayer = np.getNetworkLayerDefault();
		np.setDemandTrafficUnitsName("Mbps" , lowerLayer);
		this.upperLayer = np.addLayer("upperLayer" , "description" , "Mbps" , "upperTrafficUnits" , null);
		this.n1 = this.np.addNode(0 , 0 , "node1" , null);
		this.n2 = np.addNode(0 , 0 , "node2" , null);
		this.n3 = np.addNode(0 , 0 , "node3" , null);
		this.link12 = np.addLink(n1,n2,100,100,1,null,lowerLayer);
		this.link23 = np.addLink(n2,n3,100,100,1,null,lowerLayer);
		this.link13 = np.addLink(n1,n3,100,100,1,null,lowerLayer);
		this.d13 = np.addDemand(n1 , n3 , 3 , null,lowerLayer);
		this.d12 = np.addDemand(n1, n2, 3 , null,lowerLayer);
		this.r12 = np.addRoute(d12,1,1.5,Collections.singletonList(link12),null);
		this.path13 = new LinkedList<Link> (); path13.add(link12); path13.add(link23);
		this.r123a = np.addRoute(d13,1,1.5,path13,null);
		this.r123b = np.addRoute(d13,1,1.5,path13,null);
		this.res2 = np.addResource("type" , "name" , n2 , 100 , "Mbps" , null , 10 , null);
		this.res2backup = np.addResource("type" , "name" , n2 , 100 , "Mbps" , null , 10 , null);
		this.scd123 = np.addDemand(n1 , n3 , 3 , null,lowerLayer);
		this.scd123.setServiceChainSequenceOfTraversedResourceTypes(Collections.singletonList("type"));
		this.pathSc123 = new LinkedList<NetworkElement> (); pathSc123.add(link12); pathSc123.add(res2); pathSc123.add(link23); 
		this.sc123 = np.addServiceChain(scd123 , 100 , 300 , pathSc123 , Collections.singletonMap(res2 , 1.0) , null); 
		this.segm13 = np.addProtectionSegment(Collections.singletonList(link13) , 50 , null);
		this.upperLink12 = np.addLink(n1,n2,10,100,1,null,upperLayer);
		this.d12.coupleToUpperLayerLink(upperLink12);
	}

	@After
	public void tearDown() throws Exception 
	{
		np.checkCachesConsistency();
	}

	@Test
	public void testGetRoutes() 
	{
		Set<Route> twoRoutes = new HashSet<Route> (); twoRoutes.add (r123a); twoRoutes.add (r123b);
		Assert.assertEquals (d13.getRoutes() , twoRoutes);
		Assert.assertEquals (d12.getRoutes() , Collections.singleton(r12));
		Assert.assertEquals (scd123.getRoutes() , Collections.singleton(sc123));
		r123b.remove ();
		Assert.assertEquals (d13.getRoutes() , Collections.singleton(r123a));
	}

	@Test
	public void testGetWorseCasePropagationTimeInMs() 
	{
		Assert.assertEquals (d13.getWorseCasePropagationTimeInMs() , 200000 , 0.0);
		Assert.assertEquals (d12.getWorseCasePropagationTimeInMs() , 100000 , 0.0);
		r12.remove();
		Assert.assertEquals (d12.getWorseCasePropagationTimeInMs() , 0 , 0.0);
	}

	@Test
	public void testIsTraversingOversubscribedLinks() 
	{
		System.out.println(d12.getRoutes().iterator().next().getSeqLinksRealPath());
		System.out.println(link12.getCapacity());
		System.out.println(link12.getOccupiedCapacityIncludingProtectionSegments());
		assertTrue (d12.isTraversingOversubscribedLinks());
		assertTrue (d13.isTraversingOversubscribedLinks());
		link12.setCapacity(500);
		assertTrue (!d12.isTraversingOversubscribedLinks());
		assertTrue (d13.isTraversingOversubscribedLinks());
		link23.setCapacity(500);
		assertTrue (!d12.isTraversingOversubscribedLinks());
		assertTrue (!d13.isTraversingOversubscribedLinks());
	}

	@Test
	public void testIsTraversingOversubscribedResources() 
	{
		assertTrue (!d12.isTraversingOversubscribedResources());
		assertTrue (!scd123.isTraversingOversubscribedResources());
		sc123.setCarriedTrafficAndResourcesOccupationInformation(10 , 10 , Collections.singletonMap(res2 , 1000.0));
		assertTrue (scd123.isTraversingOversubscribedResources());
	}

	@Test
	public void testGetServiceChainSequenceOfTraversedResourceTypes() 
	{
		Assert.assertEquals(d12.getServiceChainSequenceOfTraversedResourceTypes() , Collections.emptyList());
		Assert.assertEquals(scd123.getServiceChainSequenceOfTraversedResourceTypes() , Collections.singletonList("type"));
	}

	@Test
	public void testSetServiceChainSequenceOfTraversedResourceTypes() 
	{
		try { d12.setServiceChainSequenceOfTraversedResourceTypes(Collections.singletonList("rrr")); fail ("Should fail"); } catch (Exception e) {}
		r12.remove();
		d12.setServiceChainSequenceOfTraversedResourceTypes(Collections.singletonList("rrr")); 
		Assert.assertEquals(d12.getServiceChainSequenceOfTraversedResourceTypes() , Collections.singletonList("rrr"));
	}

	@Test
	public void testGetLayer() 
	{
		Assert.assertEquals(d12.getLayer() , np.getNetworkLayerDefault());
		Assert.assertEquals(scd123.getLayer() , np.getNetworkLayerDefault());
	}

	@Test
	public void testIsBifurcated() 
	{
		assertTrue (!scd123.isBifurcated());
		assertTrue (d13.isBifurcated());
		assertTrue (!d12.isBifurcated());
	}

	@Test
	public void testIsBlocked() 
	{
		assertTrue (d12.isBlocked());
		assertTrue (d13.isBlocked());
		assertTrue (!scd123.isBlocked());
	}

	@Test
	public void testIsServiceChainRequest() 
	{
		assertTrue (!d12.isServiceChainRequest());
		assertTrue (!d13.isServiceChainRequest());
		assertTrue (scd123.isServiceChainRequest());
	}

	@Test
	public void testIsCoupled() 
	{
		assertTrue (!scd123.isCoupled());
		assertTrue (d12.isCoupled());
		assertTrue (!d13.isCoupled());
	}

	@Test
	public void testGetOfferedTraffic() 
	{
		Assert.assertEquals(d12.getOfferedTraffic() , 3 , 0.0);
		Assert.assertEquals(scd123.getOfferedTraffic() , 3 , 0.0);
	}

	@Test
	public void testGetForwardingRules() 
	{
		scd123.remove();
		np.setRoutingType(RoutingType.HOP_BY_HOP_ROUTING , lowerLayer);
		Map<Pair<Demand,Link>,Double> frs = new HashMap<Pair<Demand,Link>,Double> ();
		frs.put(Pair.of(d12 , link12) , 1.0);
		Assert.assertEquals (d12.getForwardingRules() , frs);
		frs = new HashMap<Pair<Demand,Link>,Double> ();
		frs.put(Pair.of(d13 , link12) , 1.0);
		frs.put(Pair.of(d13 , link23) , 1.0);
		Assert.assertEquals (d13.getForwardingRules() , frs);
	}

	@Test
	public void testGetCarriedTraffic() 
	{
		Assert.assertEquals (d12.getCarriedTraffic() , 1 , 0.0);
		Assert.assertEquals (d13.getCarriedTraffic() , 2 , 0.0);
		Assert.assertEquals (scd123.getCarriedTraffic() , 100 , 0.0);
	}

	@Test
	public void testGetBlockedTraffic() 
	{
		Assert.assertEquals (d12.getBlockedTraffic() , 2 , 0.0);
		Assert.assertEquals (d13.getBlockedTraffic() , 1 , 0.0);
		Assert.assertEquals (scd123.getBlockedTraffic() , 0, 0.0);
	}

	@Test
	public void testGetRoutingCycleType() 
	{
		Assert.assertEquals (d12.getRoutingCycleType() , RoutingCycleType.LOOPLESS);
		Assert.assertEquals (d13.getRoutingCycleType() , RoutingCycleType.LOOPLESS);
		Assert.assertEquals (scd123.getRoutingCycleType() , RoutingCycleType.LOOPLESS);
		scd123.remove();
		np.setRoutingType(RoutingType.HOP_BY_HOP_ROUTING , lowerLayer);
		Assert.assertEquals (d12.getRoutingCycleType() , RoutingCycleType.LOOPLESS);
		Assert.assertEquals (d13.getRoutingCycleType() , RoutingCycleType.LOOPLESS);
		np.setRoutingType(RoutingType.SOURCE_ROUTING , lowerLayer);
		Link link21 = np.addLink(n2,n1,100,100,1,null,lowerLayer);
		List<Link> path1213 = new LinkedList<Link> (); path1213.add(link12); path1213.add(link21); path1213.add(link13); 
		Route r1213 = np.addRoute(d13,1,1.5,path1213,null);
		Assert.assertEquals (d13.getRoutingCycleType() , RoutingCycleType.OPEN_CYCLES);
		np.setRoutingType(RoutingType.HOP_BY_HOP_ROUTING , lowerLayer);
		Assert.assertEquals (d13.getRoutingCycleType() , RoutingCycleType.OPEN_CYCLES);
		d12.removeAllForwardingRules();
		np.setForwardingRule(d12, link12 , 1);
		try { np.setForwardingRule(d12, link21 , 1); fail ("An exception should be here"); } catch (ClosedCycleRoutingException e) {} 
	}

	@Test
	public void testGetIngressNode() 
	{
		Assert.assertEquals (d12.getIngressNode() , n1);
		Assert.assertEquals (d13.getIngressNode() , n1);
		Assert.assertEquals (scd123.getIngressNode() , n1);
	}

	@Test
	public void testGetEgressNode() 
	{
		Assert.assertEquals (d12.getEgressNode() , n2);
		Assert.assertEquals (d13.getEgressNode() , n3);
		Assert.assertEquals (scd123.getEgressNode() , n3);
	}

	@Test
	public void testGetBidirectionalPair() 
	{
		Assert.assertEquals (d12.getBidirectionalPair() , null);
		Pair<Demand,Demand> pair = np.addDemandBidirectional(n1,n2,3,null,lowerLayer);
		Assert.assertEquals (pair.getFirst().getBidirectionalPair() , pair.getSecond());
		Assert.assertEquals (pair.getSecond().getBidirectionalPair() , pair.getFirst());
	}

	@Test
	public void testGetCoupledLink() 
	{
		Assert.assertEquals (d12.getCoupledLink() , upperLink12);
		upperLink12.remove();
		Assert.assertEquals (d12.getCoupledLink() , null);
		Assert.assertEquals (d13.getCoupledLink() , null);
	}

	@Test
	public void testCoupleToNewLinkCreated() 
	{
		try { d12.coupleToNewLinkCreated(upperLayer); fail ("Should not be here"); } catch (Exception e) {}
		try { d13.coupleToNewLinkCreated(lowerLayer); fail ("Should not be here"); } catch (Exception e) {}
		Link link13new = d13.coupleToNewLinkCreated(upperLayer);
		Assert.assertEquals (d13.getCoupledLink() , link13new);
		link13new.remove();
		Assert.assertEquals (d13.getCoupledLink() , null);
	}

	@Test
	public void testDecouple() 
	{
		d12.decouple();
		Assert.assertEquals (d12.getCoupledLink() , null);
		Link link13new = d13.coupleToNewLinkCreated(upperLayer);
		Assert.assertEquals (d13.getCoupledLink() , link13new);
		d13.decouple();
		Assert.assertEquals (d13.getCoupledLink() , null);
	}

	@Test
	public void testRemoveAllForwardingRules() 
	{
		try { d12.removeAllForwardingRules(); fail ("Bad"); } catch (Exception e) {}
		scd123.remove();
		np.setRoutingType(RoutingType.HOP_BY_HOP_ROUTING , lowerLayer);
		d12.removeAllForwardingRules();
		Assert.assertEquals (d12.getCarriedTraffic() , 0 , 0.0);
		Assert.assertEquals (d12.getForwardingRules() , Collections.emptyMap());
	}

	@Test
	public void testComputeShortestPathRoutes() 
	{
		Pair<Set<Route>,Double> sps = d12.computeShortestPathRoutes(null);
		Assert.assertEquals (sps.getFirst() , Collections.singleton(r12));
		Assert.assertEquals (sps.getSecond() , 1.0 , 0.0);
		sps = d13.computeShortestPathRoutes(null);
		Assert.assertEquals (sps.getFirst() , d13.getRoutes());
		Assert.assertEquals (sps.getSecond() , 2 , 0.0);
	}

	@Test
	public void testRemove() 
	{
		d12.remove();
		scd123.remove();
	}

	@Test
	public void testSetOfferedTraffic() 
	{
		d12.setOfferedTraffic(101);
		Assert.assertEquals (d12.getOfferedTraffic() , 101 , 0.0);
		scd123.setOfferedTraffic(101);
		Assert.assertEquals (scd123.getOfferedTraffic() , 101 , 0.0);
	}

	@Test
	public void testComputeRoutingFundamentalMatrixDemand() 
	{
		scd123.remove();
		np.setRoutingType(RoutingType.HOP_BY_HOP_ROUTING , lowerLayer);
		np.setRoutingType(RoutingType.SOURCE_ROUTING , lowerLayer);
		Assert.assertEquals(d12.getRoutes().size() , 1);
		Assert.assertEquals(d12.getRoutes().iterator().next().getSeqLinksRealPath() , Collections.singletonList(link12));
	}

}
