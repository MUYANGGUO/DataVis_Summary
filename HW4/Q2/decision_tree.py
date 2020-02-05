from util import entropy, information_gain, partition_classes
import numpy as np 
import ast

class DecisionTree(object):
    def __init__(self):
        # Initializing the tree as an empty dictionary or list, as preferred
        self.tree = []
        # self.tree = {}
        # pass

    def learn(self, X, y):
        # TODO: Train the decision tree (self.tree) using the the sample X and labels y
        # You will have to make use of the functions in utils.py to train the tree
        
        # One possible way of implementing the tree:
        #    Each node in self.tree could be in the form of a dictionary:
        #       https://docs.python.org/2/library/stdtypes.html#mapping-types-dict
        #    For example, a non-leaf node with two children can have a 'left' key and  a 
        #    'right' key. You can add more keys which might help in classification
        #    (eg. split attribute and split value)

        attr_length=len(X[0])
        y_entropy=entropy(y)
        if y_entropy==0:
            return [-1, y[0], None, None]
        
        IG_max=0
        # initialize IG with a negative value to update
        IG_max_index=-10000
        # best splits:
        best_val=[]
        best_xleft=[]
        best_yleft=[]
        best_xright=[]
        best_yright=[]

        # find best split index and split the datasets
        for index in range(attr_length):

            #determine attr type, flag true for string, false for else:
            if type(X[0][index])==str:
                flag=True
            else:
                flag=False
            
            if flag==True:
                local_val=np.unique([X[i][index] for i in range(len(X))])
                # find best split_attr
                for val in local_val:
                    X_left, X_right, y_left, y_right=partition_classes(X, y, index, val)
                    IG_update=information_gain(y, [y_left, y_right])
                    if IG_update>IG_max:
                        IG_max=IG_update
                        IG_max_index=index
                        best_val=val
                        best_xleft, best_xright, best_yleft, best_yright= X_left, X_right, y_left, y_right
            else:
                # use mean of all value to split
                local_val=np.mean([X[i][index] for i in range(len(X))])
                X_left, X_right, y_left, y_right=partition_classes(X, y, index, local_val)
                IG_update=information_gain(y,[y_left, y_right])

                if IG_update>IG_max:
                    IG_max=IG_update
                    IG_max_index=index
                    best_val=local_val
                    best_xleft, best_xright, best_yleft, best_yright= X_left, X_right, y_left, y_right
               
        if IG_max<=0:
            return [-1, np.argmax(np.bincount(y)),None, None]

        # keep going recursively    
        left=self.learn(best_xleft,best_yleft)
        right=self.learn(best_xright, best_yright)
        self.tree=[IG_max_index, best_val, left, right]
        return self.tree
        # pass


    def classify(self, record):
        # TODO: classify the record using self.tree and return the predicted label
        def _classify(record, tree):
            index, val, left, right=tree
            if index == -1:
                return val
            if type(val)==str:
                flag=True
            else:
                flag=False
            if flag:
                if record[index]==val:
                    return _classify(record, left)
                else:
                    return _classify(record, right)
            else:
                if record[index]<=val:
                    return _classify(record, left)
                else:
                    return _classify(record, right)
        return _classify(record, self.tree)
        # pass
