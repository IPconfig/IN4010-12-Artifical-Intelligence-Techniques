package ai2020.group38.ourparty;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import geniusweb.actions.Action;
import geniusweb.actions.Offer;
import geniusweb.actions.PartyId;
import geniusweb.actions.Vote;
import geniusweb.actions.Votes;
import geniusweb.inform.OptIn;
import geniusweb.inform.Settings;
import geniusweb.inform.Voting;
import geniusweb.inform.YourTurn;
import geniusweb.issuevalue.Bid;
import geniusweb.issuevalue.DiscreteValue;
import geniusweb.issuevalue.NumberValue;
import geniusweb.issuevalue.Value;
import geniusweb.profile.Profile;
import geniusweb.profile.utilityspace.LinearAdditiveUtilitySpace;
import geniusweb.progress.ProgressRounds;
import geniusweb.references.Parameters;
import geniusweb.references.ProfileRef;
import geniusweb.references.ProtocolRef;

public class TimeDependentPartyMOPACTest {

	private static final PartyId me = new PartyId("me");
	private static final PartyId otherparty = new PartyId("other");
	private static final String MOPAC = "MOPAC";
	private static final String PROFILE = "src/test/resources/testprofile.json";
	private final static ObjectMapper jackson = new ObjectMapper();

	private TimeDependentParty party;
	private TestConnection connection = new TestConnection();
	private ProtocolRef protocol = new ProtocolRef("MOPAC");
	private ProgressRounds progress = mock(ProgressRounds.class);
	private Parameters parameters = new Parameters();
	private Settings settings;
	private LinearAdditiveUtilitySpace profile;
	private Map<PartyId, Integer> powers = new HashMap<>();
	private Bid bestBid;

	@Before
	public void before() throws JsonParseException, JsonMappingException,
			IOException, URISyntaxException {
		powers.put(me, 1);
		powers.put(otherparty, 1);

		party = new TimeDependentParty() {

			@Override
			public String getDescription() {
				return "test";
			}

			@Override
			public double getE() {
				return 2; // conceder-like
			}
		};
		settings = new Settings(me, new ProfileRef(new URI("file:" + PROFILE)),
				protocol, progress, parameters);

		String serialized = new String(Files.readAllBytes(Paths.get(PROFILE)),
				StandardCharsets.UTF_8);
		profile = (LinearAdditiveUtilitySpace) jackson.readValue(serialized,
				Profile.class);

		// hard coded best bid for this domain
		Map<String, Value> vals = new HashMap<>();
		vals.put("issue2", new NumberValue(new BigDecimal("18")));
		vals.put("issue1", new DiscreteValue("issue1value2"));
		bestBid = new Bid(vals);

	}

	@Test
	public void testPartyFirstOffers() {
		party.connect(connection);
		party.notifyChange(settings);

		party.notifyChange(new YourTurn());
		assertEquals(1, connection.getActions().size());
		assertTrue(connection.getActions().get(0) instanceof Offer);
	}

	@Test
	public void checkPartyFirstBid() {
		party.connect(connection);
		party.notifyChange(settings);

		// let party make an offer and go to the voting phase
		party.notifyChange(new YourTurn());
		Offer offer = (Offer) connection.getActions().get(0);

		// it's either the best or one-but-best bid,
		// because we set up the tolerance like that.
		Bid bid = offer.getBid();
		assertEquals("issue1value2",
				((DiscreteValue) bid.getValue("issue1")).getValue());
		assertTrue(((NumberValue) bid.getValue("issue2")).getValue()
				.compareTo(new BigDecimal("17")) >= 0);
	}

	@Test
	public void testPartyVotesForBest() {
		party.connect(connection);
		party.notifyChange(settings);

		// let party make an offer and go to the voting phase
		party.notifyChange(new YourTurn());
		assertEquals(1, connection.getActions().size());

		// send bestBid bid as the voting option
		// this is better than party's firstBid

		party.notifyChange(new Voting(
				Arrays.asList(new Offer(otherparty, bestBid)), powers));
		assertEquals(2, connection.getActions().size());

		Action lastAction = connection.getActions().get(1);
		assertTrue(lastAction instanceof Votes);
		assertEquals(
				new Votes(me,
						Collections.singleton(
								new Vote(me, bestBid, 1, Integer.MAX_VALUE))),
				lastAction);
	}

	@Test
	public void testPartyOptsInBest() {
		party.connect(connection);
		party.notifyChange(settings);

		// let party make an offer and go to the voting phase
		party.notifyChange(new YourTurn());
		assertEquals(1, connection.getActions().size());

		// send bestBid bid as the voting option
		// this is better than party's firstBid

		party.notifyChange(new Voting(Arrays.asList(new Offer(me, bestBid),
				new Offer(otherparty, bestBid)), powers));
		assertEquals(2, connection.getActions().size());

		// we already checked the party votes for bestBid
		Votes votes = (Votes) connection.getActions().get(1);

		party.notifyChange(new OptIn(Arrays.asList(votes)));
		assertEquals(3, connection.getActions().size());

		Action lastAction = connection.getActions().get(2);
		assertTrue(lastAction instanceof Votes);
		assertTrue(((Votes) lastAction).isExtending(votes));
	}

	@Test
	public void testGetCapabilities() {
		assertTrue(party.getCapabilities().getBehaviours().contains(MOPAC));
	}

//	private Bid findBestBid() {
//		BigDecimal bestvalue = BigDecimal.ZERO;
//		Bid best = null;
//		for (Bid bid : new AllBidsList(profile.getDomain())) {
//			BigDecimal util = profile.getUtility(bid);
//			if (util.compareTo(bestvalue) > 0) {
//				best = bid;
//				bestvalue = util;
//			}
//		}
//		return best;
//	}

}
