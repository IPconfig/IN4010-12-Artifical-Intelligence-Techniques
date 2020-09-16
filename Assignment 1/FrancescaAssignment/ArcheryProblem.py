from Class_Bayes import Bayes

# Initialize values

list_hypo = ["Beginner", "Intermediate", "Advanced", "Expert"]
list_priors = [0.25, 0.25, 0.25, 0.25]
list_obs = ["Yellow", "Red", "Blue", "Black", "White"]
# list_likelihood[0][1] corresponds to Beginner and Red, or 0.2
list_likelihood =   [[0.05, 0.1, 0.4, 0.25, 0.2],
                    [0.1, 0.2, 0.4, 0.2, 0.1],
                    [0.2, 0.4, 0.25, 0.1, 0.05],
                    [0.3, 0.5, 0.125, 0.05, 0.025]]

b = Bayes(list_hypo, list_priors, list_obs, list_likelihood)
observed = ["Yellow", "White", "Blue", "Red", "Red", "Blue"]

prevState = list_priors
for o in observed:
    prevState = b.single_posterior_update(o, prevState)
print(prevState)

# Question 3: The likelihood of the archer being intermediate is 0.13.
# Question 4: It is most likely that the archer is advanced.



