class Bayes:
    'Class for mathematical operations using Bayes rule'
    def __init__(self, hypos, priors, obs, probabilities):
        """
        The constructor for Bayes class.

        Parameters:
            hypos (list): list of hypotheses
            priors (list): list of priors of the hypotheses
            obs (list): list of possible observations
            likelihood (array): A double array returning the probability. The first index is for the hypothesis, the second index for the observation
        """
        self.hypos = hypos
        self.priors = priors
        self.obs = obs
        self.probabilities = probabilities

    def likelihood(self, observation, hypothesis):
        """
        Returns the probability of the observation under the assymption that the hypothesis is true P(O|H).

        Parameters:
            observation (String): one observation from the list of observations
            hypothesis (String): one hypothesis from the list of hypotheses

        Returns:
            Float: likelihood P(O|H)
        """
        try:
            indexObs = self.obs.index(observation)
            indexHypos = self.hypos.index(hypothesis)
            return self.probabilities[indexHypos][indexObs]
        except ValueError:
            print("Observation or Hypothesis value not found in list")
            return None

    def norm_constant(self, observation):
        """
        Returns the probability of the observation under any hypothesis.

        Parameters:
            observation (String): one observation from the list of observations

        Returns:
            Float: Normalizing constant P(O)
        """
        constant = 0
        for hypothesis, prior in zip(self.hypos, self.priors):
            probability = prior * self.likelihood(observation, hypothesis)
            constant += probability
        return constant

    def single_posterior_update(self, observation, priors):
        """
        Calculates the probability of the hypothesis after one observation

        Parameters:
            observation (String): one observation from the list of observations.
            priors (list): probability of hypothesis before any observations

        Returns:
            List: list of posterior probabilities
        """
        posterior = []
        for hypothesis, prior in zip(self.hypos, priors):
            likelihood = self.likelihood(observation, hypothesis)
            normalization = self.norm_constant(observation)
            post = (prior * likelihood) / normalization
            posterior.append(post)
        return posterior

    def compute_posterior(self, observations):
        """
        Calculates the posterior probabilites based on a list of observations.

        Parameters:
            observations (String): List of observations

        Returns:
            List: list of posterior probabilities per hypothesis
        """
        posteriors = self.priors  # there are no observations made yet
        for observation in observations:
            posteriors = self.single_posterior_update(observation, posteriors)
            self.priors = posteriors  # update self.priors to previous computed posteriors
        return posteriors
