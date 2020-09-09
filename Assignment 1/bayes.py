class Bayes:
    'Class for mathematical operations using Bayes rule'
    def __init__(self, hypos, priors, obs, probabilities):
        """
        The constructor for Bayes class.

        Parameters:
            hypos (list): list of hypotheses
            priors (list): list of priors of the hypotheses
            obs (list): list of possible observations
            likelihood (array): A double array returning the probability. The first index is for the hypotheses, the second index for the observation
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
    
    def norm_constant(self, observation, priors=None):
        """
        Returns the probability of the observation under any hypothesis P(O).

        Parameters:
            observation (String): one observation from the list of observations
            priors (List): list of priors. Optional, default is self.priors

        Returns:
            Float: Normalizing constant P(O)
        """
        # is created by summing over each hypothesis where
        # total probability of hypothesis * conditional probability of observation given hypothesis
        constant = 0
        if priors is None:
            priors = self.priors
        for hypothesis, prior in zip(self.hypos, priors):
            # TODO: are we allowed to use priors here? or should we calculate based on prob array
            probability = prior * self.likelihood(observation, hypothesis)
            constant += probability
        return constant

    # TODO: excerise 4
    def single_posterior_update(self, observation, priors):
        """
        Calculates the probability of the hypothesis after an observation P(H|O)

        Parameters:
            observation (String): one observation from the list of observations. e.g. vanilla
            priors (list): probability of hypothesis before any observations P(H) e.g. [0.5, 0.5]

        Returns:
            List: list of posterior probabilities
        """
        posterior = []
        for prior in priors:
            # INCORRECT
            likelihood = self.likelihood(hypothesis=observation, prior)
            normalization = self.norm_constant(observation)
            post = (prior * likelihood) / normalization
            posterior.append(post)
        return posterior

    # TODO: Check implementation of exercise 5
    def compute_posterior(self, observations):
        """
        Calculates the posterior probabilites based on an observation. Priors are already given

        Parameters:
            observations (String): List of observation from the list of observations

        Returns:
            List: list of posterior probabilities
        """
        posteriors = []
        for observation in observations:
            posteriors.append(self.single_posterior_update(observation, self.priors))
            return posteriors