import sys

from nltk import tokenize

# from nltk.stem import WordNetLemmatizer

import csv

import codecs, locale



sys.stdout = codecs.getwriter('utf8')(sys.stdout)

sys.stderr = codecs.getwriter('utf8')(sys.stderr)



#{hedge:type}

dictionary = {}

#{hedge_start:[[hedge,type,[tokenized]]]}

multiword_dictionary = {}

# wordnet_lemmatizer = WordNetLemmatizer()


def check_hedge_pos(token, pos):
    if token == 'like':
        return pos[0] != 'v'
    if token == 'about':
        return pos[0] != 'i'
    if token == 'might' or token == 'may':
        return pos[0] != 'n'
    """
    # Seth's additions
    if token == 'around':
        return pos == 'in'
    if token == 'kinda':
        return pos[:2] == 'rb'
    if token == 'likely':
        return pos[:2] == 'rb' # this could be weird - not sure about "That is likely to happen."
    if token == 'may':
        return pos[0] != 'n' # not sufficient - just excludes month
    if token == 'might':
        return pos[0] != 'n' # not sufficient - just excludes might==strength
    if token == 'pretty':
        return pos[0] != 'j'
    """

    return True
    
def check_hedge_next(lemma, next):
    #if token == 'supposed':
    #    return next != 'to'
    #if lemma == 'find':
    #    return next != 'out'
    return True

def check_hedge_deps(lemma, dependencies):
    if lemma == 'appear' or lemma == 'assume':
        for relation, head, dependent in dependencies:
            if head == lemma and (relation == 'xcomp' or relation == 'ccomp'):
                return True
        return False
    if lemma == 'believe':
        neg = False # if "believe" is negated
        aux = False # if "believe" in this sentence has a modal as a dependent
        sub_clause = False
        for relation, head, dependent in dependencies:
            if head == lemma:
                if relation == 'aux': # ASK: should we limit the modal? is "i wouldn't believe" a hedge??
                    aux = True
                elif relation == 'neg':
                    neg = True
                elif relation == 'ccomp' or relation == 'xcomp':
                    sub_clause = True
        # 'believe' is only a hedge if: it has a subordinate clause, and isn't used with a negated modal
        return sub_clause and not (aux and neg)

    return True

def find_hedges(sentences, all_deps):
    #for sent in sentences:
    for s in range(len(sentences)):
        sent = sentences[s]
        sent_deps = all_deps[s]
        #if sent[len(sent) - 1] != '?':
        for i in range(len(sent)):
            token = sent[i][0].lower()
            pos = sent[i][1].lower()
            lemma = sent[i][2].lower()
            # print lemma
            if token in multiword_dictionary:
                hedge = multiword_dictionary[token]
                for j in range(len(hedge)):
                    match = True
                    for k in range(len(hedge[j][2])):
                        if i + k >= len(sent) or sent[i + k][0].lower() != hedge[j][2][k] or sent[i+k][3] != '_':
                            match = False
                    if match:
                        for k in range(len(hedge[j][2])):
                            sent[i+k][3] = 'M' + str(k) + '\t1\t' + hedge[j][1]
            if token in dictionary and sent[i][3] == '_':
                pos_ok = check_hedge_pos(token, pos)
                deps_ok = check_hedge_deps(lemma, sent_deps)
                next_ok = True
                if i + 1 < len(sent):
                    next_ok = check_hedge_next(lemma, sent[i+1][0].lower())
                if pos_ok and next_ok and deps_ok:
                    sent[i][3] = 'S\t1\t' + dictionary[token]

            
def read_dictionary():
    """
    with open('hedge/multiword.txt') as f:
        reader = f.readlines()
        for line in reader:
            line = line.strip()
            #[[hedge,type,def_type,def_hedge,ex_hedge,def_not,ex_not,stem,prob]]
            tokenized = tokenize.word_tokenize(line.lower())
            if tokenized[0] not in multiword_dictionary:
                multiword_dictionary[tokenized[0]] = []
            multiword_dictionary[tokenized[0]].append([line, 'multiword', tokenized])
    """
    with open('hedgeClassifier/multiword_dict.csv', 'rU') as f:
        reader = csv.DictReader(f, delimiter='\t')
        for row in reader:
            hedge = row['Hedge'].strip().lower()
            tokenized = tokenize.word_tokenize(hedge)
            if tokenized[0] not in multiword_dictionary:
                multiword_dictionary[tokenized[0]] = []
            multiword_dictionary[tokenized[0]].append([hedge, 'multiword', tokenized])
    """
    with open('hedge/hProp.txt') as f:
        reader = f.readlines()
        for line in reader[1:]:
            line = line.strip()
            #print line
            dictionary[line.lower()] = 'hProp'
    with open('hedge/hRel.txt') as f:
        reader = f.readlines()
        for line in reader[1:]:
            line = line.strip()
            dictionary[line.lower()] = 'hRel'
    """
    with open('hedgeClassifier/dictionary.csv', 'rU') as f:
        reader = csv.DictReader(f, delimiter='\t')
        for row in reader:
            dictionary[row['Hedge'].strip().lower()] = row['Type'].strip()

def read_sentences(tokens):
    sentences = []
    sentence = []
    for token in tokens:
        trimmed = token.strip()
        if len(trimmed) == 0:
            if len(sentence) > 0:
                sentences.append(sentence)
                sentence = []
        else:
            trimmed_split = trimmed.split('\t')
            word = trimmed_split[0]
            tag = trimmed_split[1]
            lemma = trimmed_split[2]
            #print word
            #print tag
            #lemma = wordnet_lemmatizer.lemmatize(word,tag[0].lower()) if tag[0].lower() in ['a','n','v'] else wordnet_lemmatizer.lemmatize(word)
            #print lemma
            sentence.append([word,tag,lemma,'_'])
    if len(sentence) > 0:
        sentences.append(sentence)
    return sentences

def read_dependencies(lines):
    all_deps = []
    sentence_deps = []
    for line in lines:
        trimmed = line.strip()
        if len(trimmed) == 0:
            if len(sentence_deps) > 0:
                all_deps.append(sentence_deps)
                sentence_deps = []
            continue
        if trimmed == 'NO DEPS':
            all_deps.append([])
            continue
        sentence_deps.append(trimmed.split('\t'))
    if len(sentence_deps) > 0:
        all_deps.append(sentence_deps)
    return all_deps


def print_tagged(sentences):
    for sentence in sentences:
        for token in sentence:
            print token[0] + '\t' + token[3]
        print

if __name__=="__main__":
    tokens_file = codecs.open(sys.argv[1],encoding='utf-8')
    tokens = tokens_file.readlines()
    sentences = read_sentences(tokens)

    deps_file = codecs.open(sys.argv[2],encoding='utf-8')
    all_deps = read_dependencies(deps_file.readlines())
    read_dictionary()

    find_hedges(sentences, all_deps)
    print_tagged(sentences)
    