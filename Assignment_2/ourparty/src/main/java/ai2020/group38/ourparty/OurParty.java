package ai2020.group38.ourparty;

import geniusweb.actions.*;
import geniusweb.inform.*;
import geniusweb.issuevalue.Bid;
import geniusweb.party.Capabilities;
import geniusweb.party.DefaultParty;
import geniusweb.profile.Profile;
import geniusweb.profile.utilityspace.LinearAdditive;
import geniusweb.profile.utilityspace.UtilitySpace;
import geniusweb.profileconnection.ProfileConnectionFactory;
import geniusweb.profileconnection.ProfileInterface;
import geniusweb.progress.Progress;
import geniusweb.progress.ProgressRounds;
import tudelft.utilities.immutablelist.ImmutableList;
import tudelft.utilities.logging.Reporter;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.*;
import java.util.logging.Level;
import java.util.stream.Collectors;

/**
 * General time dependent party. This is a simplistic implementation that does
 * brute-force search through the bidspace and can handle bidspace sizes up to
 * 2^31 (approx 1 billion bids). It may take excessive time and run out of time
 * on bidspaces &gt; 10000 bids. In special cases it may even run out of memory,
 *
 * <p>
 * Supports parameters as follows
 * <table summary="parameters">
 * <tr>
 * <td>e</td>
 * <td>e determines how fast the party makes concessions with time. Typically
 * around 1. 0 means no concession, 1 linear concession, &gt;1 faster than
 * linear concession.</td>
 * </tr>
 *
 * <tr>
 * <td>minPower</td>
 * <td>This value is used as minPower for placed {@link Vote}s. Default value is
 * 1.</td>
 * </tr>
 *
 * <tr>
 * <td>maxPower</td>
 * <td>This value is used as maxPower for placed {@link Vote}s. Default value is
 * infinity.</td>
 * </tr>
 *
 * </table>
 * <p>
 * TimeDependentParty requires a {@link UtilitySpace}
 */
public class OurParty extends DefaultParty {

	private ProfileInterface profileint;
	private LinearAdditive utilspace = null; // last received space
	private PartyId me;
	private Progress progress;
	private Bid lastReceivedBid = null;
	private ExtendedUtilSpace extendedspace;
	private double e = 1.2;
	private Votes lastvotes;
	private Settings settings;
	private Bid previousBid;
	private BigDecimal tolerance;

	public OurParty() {	super();}

	public OurParty(Reporter reporter) {
		super(reporter); // for debugging
	}

	@Override
	public Capabilities getCapabilities() {
		return new Capabilities(
				new HashSet<>(Arrays.asList("SAOP", "AMOP", "MOPAC")),
				Collections.singleton(Profile.class));
	}

	@Override
	public void notifyChange(Inform info) {
		try {
			if (info instanceof Settings) {
				settings = (Settings) info;
				this.profileint = ProfileConnectionFactory
						.create(settings.getProfile().getURI(), getReporter());
				this.me = settings.getID();
				this.progress = settings.getProgress();
				Object newe = settings.getParameters().get("e");
				if (newe != null) {
					if (newe instanceof Double) {
						this.e = (Double) newe;
					} else {
						getReporter().log(Level.WARNING,
								"parameter e should be Double but found "
										+ newe);
					}
				}
			} else if (info instanceof ActionDone) {
				Action otheract = ((ActionDone) info).getAction();
				if (otheract instanceof Offer) {
					lastReceivedBid = ((Offer) otheract).getBid();
				}
			} else if (info instanceof YourTurn) {
				myTurn();
			} else if (info instanceof Finished) {
				getReporter().log(Level.INFO, "Final outcome:" + info);
			} else if (info instanceof Voting) {
				lastvotes = vote((Voting) info);
				getConnection().send(lastvotes);
			} else if (info instanceof OptIn) {
				lastvotes = optIn((OptIn) info);
				getConnection().send(lastvotes);
			}
		} catch (Exception ex) {
			getReporter().log(Level.SEVERE, "Failed to handle info", ex);
		}
		updateRound(info);
	}

	/**
	 * @return the E value that controls the party's behaviour. Depending on the
	 *         value of e, extreme sets show clearly different patterns of
	 *         behaviour [1]:
	 *
	 *         1. Boulware: For this strategy e &lt; 1 and the initial offer is
	 *         maintained till time is almost exhausted, when the agent concedes
	 *         up to its reservation value.
	 *
	 *         2. Conceder: For this strategy e &gt; 1 and the agent goes to its
	 *         reservation value very quickly.
	 *
	 *         3. When e = 1, the price is increased linearly.
	 *
	 *         4. When e = 0, the agent plays hardball.
	 */
	public double getE() {
		return e;
	}

	@Override
	public String getDescription() {
		return "Time-dependent conceder. Aims at utility u(t) = scale * t^(1/e) "
				+ "where t is the time (0=start, 1=end), e is the concession speed parameter (default 1.1), and scale such that u(0)=minimum and "
				+ "u(1) = maximum possible utility. Parameters minPower (default 1) and maxPower (default infinity) are used "
				+ "when voting";
	}

	/******************* private support funcs ************************/

	/**
	 * Update {@link #progress}, depending on the protocol and last received
	 * {@link Inform}
	 *
	 * @param info the received info.
	 */
	private void updateRound(Inform info) {
		if (settings == null) // not yet initialized
			return;
		String protocol = settings.getProtocol().getURI().getPath();

		switch (protocol) {
		case "SAOP":
		case "SHAOP":
			if (!(info instanceof YourTurn))
				return;
			break;
		case "MOPAC":
			if (!(info instanceof OptIn))
				return;
			break;
		default:
			return;
		}
		// if we get here, round must be increased.
		if (progress instanceof ProgressRounds) {
			progress = ((ProgressRounds) progress).advance();
		}

	}

	private void myTurn() throws IOException {
		updateUtilSpace();
		Bid bid = makeBid();

		Action myAction;
		if (bid == null || (lastReceivedBid != null
				&& utilspace.getUtility(lastReceivedBid)
						.compareTo(utilspace.getUtility(bid)) >= 0)) {
			// if bid==null we failed to suggest next bid.
			myAction = new Accept(me, lastReceivedBid);
		} else {
			myAction = new Offer(me, bid);
		}
		getConnection().send(myAction);

	}

	private LinearAdditive updateUtilSpace() throws IOException {
		Profile newutilspace = profileint.getProfile();
		if (!newutilspace.equals(utilspace)) {
			utilspace = (LinearAdditive) newutilspace;
			extendedspace = new ExtendedUtilSpace(utilspace);
			tolerance = extendedspace.computeTolerance();
		}
		return utilspace;
	}

	/**
	 * @return next possible bid with current target utility, or null if no such
	 *         bid.
	 */
	private Bid makeBid() {
		//double time = progress.get(System.currentTimeMillis());
		Profile p;
		try {
			p = profileint.getProfile();
		} catch (IOException ex) {
			throw new IllegalStateException(ex);
		}
		if (previousBid == null) {
			previousBid = extendedspace.getBids(extendedspace.getMax()).get(0);
			return previousBid;
		} else {		// Decrease in steps of tolerance to propose a slightly worse offer
			ImmutableList<Bid> options = extendedspace.getBids(((UtilitySpace) p).getUtility(previousBid).subtract(tolerance));
			int c = 2;
			while (options.size() == BigInteger.ZERO) {
				options = extendedspace.getBids(((UtilitySpace) p).getUtility(previousBid).subtract(tolerance.multiply(new BigDecimal(c))));
				c++;
			}
			previousBid = options.get(0);
			return previousBid;
		}

	}

	/**
	 *
	 * @param t       the time in [0,1] where 0 means start of nego and 1 the
	 *                end of nego (absolute time/round limit)
	 * @param e       the e value that determinses how fast the party makes
	 *                concessions with time. Typically around 1. 0 means no
	 *                concession, 1 linear concession, &gt;1 faster than linear
	 *                concession.
	 * @param minUtil the minimum utility possible in our profile
	 * @param maxUtil the maximum utility possible in our profile
	 * @return the utility goal for this time and e value
	 */
	protected BigDecimal getUtilityGoal(double t, double e, BigDecimal minUtil,
			BigDecimal maxUtil) {

		BigDecimal ft1 = BigDecimal.ONE;
		if (e != 0)
			ft1 = BigDecimal.valueOf(1 - Math.pow(t, 1 / e)).setScale(6,
					RoundingMode.HALF_UP);
		return minUtil.add((maxUtil.subtract(minUtil).multiply(ft1)))
				.min(maxUtil).max(minUtil);
	}

	/**
	 * @param voting the {@link Voting} object containing the options
	 *
	 * @return our next Votes.
	 */
	private Votes vote(Voting voting) throws IOException {
		Object val = settings.getParameters().get("minPower");

		int sum = 0;

		for (int i : voting.getPowers().values()) {
			sum += i;
		}
		// max utility requires smallest possible group/power
		Integer minpower = (val instanceof Integer) ? (Integer) val : (int) Math.floor(sum / 2.0 + 1);
		val = settings.getParameters().get("maxPower");
		Integer maxpower = (val instanceof Integer) ? (Integer) val
				: sum;
		Set<Vote> votes = voting.getBids().stream().distinct()
				.filter(offer -> isGood(offer.getBid()))
				.map(offer -> new Vote(me, offer.getBid(), minpower, maxpower))
				.collect(Collectors.toSet());
		return new Votes(me, votes);
	}

	/**
	 *
	 * @param voting the votes from all the parties
	 * @return our updated votes after opt-in phase
	 */
	private Votes optIn(OptIn voting) {
		List<Votes> votesList = voting.getVotes();
		Set<Vote> resultSet = lastvotes.getVotes();
		Profile profile;
		try {
			profile = profileint.getProfile();
		} catch (IOException ex) {
			throw new IllegalStateException(ex);
		}
		for (Votes v : votesList) { // every Votes object is a list of votes from 1 party
			for (Vote v2 : v.getVotes()) { // goes through all votes from all parties, check if we didnt accept before and their is consensus and utility is good enough.
				if (!accepted(v2.getBid()) && ((UtilitySpace) profile).getUtility(v2.getBid()).compareTo(extendedspace.getMax().multiply(new BigDecimal("0.50"))) >= 0
						&& checkConsensus(voting, v2.getBid())) {
					resultSet.add(new Vote(me, v2.getBid(), 1, 9999999));
				}
			}
		}
		return new Votes(me, resultSet);
	}

	/**
	 *
	 * @param bid the bid to check
	 * @return true iff we accepted this bid in our voting phase.
	 */
	private boolean accepted(Bid bid) {
		for (Vote v : lastvotes.getVotes()) {
			if (v.getBid().equals(bid))
				return false;
		}
		return true;
	}

	/**
	 * @param voting the votes from all parties
	 * @param bid the bid to check
	 * @return true iff there is a consensus among other parties
	 */
	private boolean checkConsensus(OptIn voting, Bid bid) {
		int power = 0;
		int minthreshold = 0;
		for (Votes v : voting.getVotes()) {
			if (v.getVote(bid) != null) {
				power += 1;
				if (v.getVote(bid).getMinPower() > minthreshold) {
					minthreshold = v.getVote(bid).getMinPower();
				}
			}

		}
		return power >= minthreshold;
	}

	/**
	 * @param bid the bid to check
	 * @return true iff bid is good for us.
	 */
	private boolean isGood(Bid bid) {
		if (bid == null || profileint == null)
			return false;
		Profile profile;
		try {
			profile = profileint.getProfile();
		} catch (IOException ex) {
			throw new IllegalStateException(ex);
		}
		// the profile MUST contain UtilitySpace
//		double time = progress.get(System.currentTimeMillis());
		return ((UtilitySpace) profile).getUtility(bid)
				.compareTo(extendedspace.getMax().multiply(new BigDecimal("0.7"))) >= 0;

	}
}
