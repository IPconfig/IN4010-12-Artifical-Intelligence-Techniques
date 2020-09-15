from bayes import Bayes

hypos = ["Bowl1", "Bowl2"]
priors = [0.5, 0.5]
obs = ["chocolate", "vanilla"]
# e.g. likelihood[0][1] corresponds to the likehood of Bowl1 and vanilla, or 35/50
likelihood = [[15/50, 35/50], [30/50, 20/50]]

b = Bayes(hypos, priors, obs, likelihood)

l = b.likelihood("chocolate", "Bowl1")
print("likelihood(chocolate, Bowl1) = %s " % l)

n_c = b.norm_constant("vanilla")
print("normalizing constant for vanilla: %s" % n_c)

p_1 = b.single_posterior_update("vanilla", [0.5, 0.5])
print("vanilla - posterior: %s" % p_1)

p_2 = b.compute_posterior(["chocolate", "vanilla"])
print("chocolate, vanilla - posterior: %s" % p_2)


# Archery Problem
arch_hypothesis = ["Beginner", "Intermediate", "Advanced", "Expert"]
arch_priors = [0.25, 0.25, 0.25, 0.25]
arch_observations = ["Yellow", "Red", "Blue", "Black", "White"]
arch_Likelihood = [[0.05, 0.1, 0.4, 0.25, 0.2],
                   [0.1, 0.2, 0.4, 0.2, 0.1],
                   [0.2, 0.4, 0.25, 0.1, 0.05],
                   [0.3, 0.5, 0.125, 0.05, 0.025]]

# Initialize new Bayes instance
bay = Bayes(arch_hypothesis, arch_priors, arch_observations, arch_Likelihood)

p_3 = bay.compute_posterior(["Yellow", "White", "Blue", "Red", "Red", "Blue"])
print("distribution for yellow, white, blue, red, red, blue is: %s" % p_3)
