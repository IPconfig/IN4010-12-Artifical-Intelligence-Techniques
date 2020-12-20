import numpy as np

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
        self.num_states = num_states
        self.num_actions = num_actions
        self.q_table = np.zeros((self.num_states, self.num_actions))

    def process_experience(self, state, action, next_state, reward, done): # You can add more arguments if you want
        """
        Update the Q-value based on the state, action, next state and reward.
        np.max(self.q_table[next_state, :]) gives us the maximum value of Q for the next state.
        argmax gives the index of the max.
        """

        if not done:
            self.q_table[state, action] = (1-LEARNINGRATE) * self.q_table[state, action] + \
                LEARNINGRATE * (reward + DEFAULT_DISCOUNT * np.argmax(self.q_table[next_state, :]))
        else: 
            self.q_table[state, action] = (1-LEARNINGRATE) * self.q_table[state, action] + LEARNINGRATE * reward

    def select_action(self, state, random_action): # You can add more arguments if you want
        """
        Returns an action, selected based on the current state
        """
        # if the random chosen value between 0-1 is less than e
        # choose the most promising value from Q-table for this state.
        if np.random.random() > EPSILON:
            biggest_values = np.argwhere(self.q_table[state, :] == np.amax(self.q_table[state, :]))
            biggest_values = biggest_values.flatten().tolist()
            return np.random.choice(biggest_values)
        # Otherwise choose a random action. 
            # from the state "state" pick a random action between 0 to 3.
            # return self.q_table[state,np.random.randint(4)]
        return random_action


    def report(self):
        """
        Function to print useful information, printed during the main loop
        """
        print('---')