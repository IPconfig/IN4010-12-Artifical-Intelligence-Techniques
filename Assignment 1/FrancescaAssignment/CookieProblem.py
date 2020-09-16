from Class_Bayes import Bayes

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

# Question 1: The probability that the cookie came from Bowl 1 is 63%.

p_2 = b.compute_posterior(["chocolate", "vanilla"])
print("chocolate, vanilla - posterior: %s" % p_2)

# Question 2: The probability of one chocolate cookie being from Bowl 2 is 2/3.
# The probability of one vanilla cookie being from Bowl 2 is 0.36.
# Thus, the probabilility of one vanillia and one chocolate cookie coming from Bowl 2 is: 2/3 * 0,36 = 6/25 = 0.24.
# I think I can calculate it that way, because taking one chocolate cookie
# wont influence the probability that next time I get a vanillia cookie from the bowl and vice versa.