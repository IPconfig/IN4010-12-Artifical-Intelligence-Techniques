class Bayes:

    def __init__(self, list_hypo, list_priors, list_obs, list_likelihood):
        """ Initialized with a (1) list of hypothesis, (2) a list with the priors of the hypothesis,
            (3) a list of possible observations and (4) a likelihood array"""

        self.list_hypo = list_hypo
        self.list_priors = list_priors
        self.list_obs = list_obs
        self.list_likelihood = list_likelihood

    def likelihood(self, obs, hyp):
        """ Gets an observation and hypothesis and returns likelihood """
        return self.list_likelihood[self.list_hypo.index(hyp)][self.list_obs.index(obs)]

    def norm_constant(self, obs):
        """ returns normalized constant given a specific observation
            P(O) = P(O|H1)P(H1)+P(O|H2)P(H2)
        """
        norm_cons = 0
        for i, h in enumerate(self.list_hypo):
            norm_cons += self.likelihood(obs, h) * self.list_priors[i]
        return norm_cons

    def single_posterior_update(self, obs, priors):
        """Returns the posterior probabilities of an observation obs
            P(A|B)=P(B|A)P(A)/P(B)
        """
        posterior_probs = []
        for i, hyp in enumerate(self.list_hypo):
            posterior_probs.append((self.likelihood(obs, hyp) * priors[i])/self.norm_constant(obs))
        return posterior_probs

    def compute_posterior(self, list_obs):
        """Gets a list of independent and identically distributed observations and
        returns the posterior probabilities."""
        posterior_probs = []
        for obs in list_obs:
            posterior_probs.append(self.single_posterior_update(obs, self.list_priors))
        return posterior_probs