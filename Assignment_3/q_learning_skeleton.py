import numpy as np
import random

NUM_EPISODES = 1000
MAX_EPISODE_LENGTH = 500

DEFAULT_DISCOUNT = 0.9
EPSILON = 0.05
LEARNINGRATE = 0.1


class QLearner():
    """
    Q-learning agent
    """
    def __init__(self, num_states, num_actions, discount=DEFAULT_DISCOUNT, learning_rate=LEARNINGRATE): # You can add more arguments if you want
        self.name = "agent1"
        self.discount = discount
        self.learning_rate = learning_rate
        self.ql = np.zeros((num_states, num_actions))

    def process_experience(self, state, action, next_state, reward, done): # You can add more arguments if you want
        """
        Update the Q-value based on the state, action, next state and reward.
        """
        if not done:  
            self.ql[state, action] = self.ql[state, action] * (1 - LEARNINGRATE) + LEARNINGRATE * (reward + DEFAULT_DISCOUNT * np.argmax(self.ql[next_state, :]))
        else:
            self.ql[state, action] = self.ql[state, action] * (1 - LEARNINGRATE) + LEARNINGRATE * reward

    def select_action(self, state): # You can add more arguments if you want
        """
        Returns an action, selected based on the current state
        """
        if random.uniform(0, 1) < EPSILON:
            """
            Explore: select a random action
            """
            action = random.randint(0,3)
        else:
            """
            Exploit: select the action with max value (future reward)
            If there are multiple max values, return a random action
            """
            biggest_values = np.argwhere(self.ql[state, :] == np.max(self.ql[state, :]))
            biggest_values = biggest_values.flatten().tolist()
            action = np.random.choice(biggest_values)

        return action




    def report(self):
        """
        Function to print useful information, printed during the main loop
        """
        print("---")
    #    print(self.ql)
